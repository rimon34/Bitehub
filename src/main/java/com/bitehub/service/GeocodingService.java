package com.bitehub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves free-text addresses to latitude/longitude using a keyless,
 * OpenStreetMap-based provider (Photon by default, Nominatim as an alternative).
 *
 * Design goals:
 *  - Never break the caller: any failure simply returns an empty result and the
 *    map falls back to its simulated coordinates.
 *  - Be a good citizen: results are cached, outbound requests are rate limited,
 *    and a real User-Agent is always sent (Nominatim requires this).
 *  - Stay on the map: hits further than a configurable radius from the campus
 *    base point are rejected, so a fictional address can never drop a pin on the
 *    other side of the planet.
 */
@Service
public class GeocodingService {

    private static final Logger log = LoggerFactory.getLogger(GeocodingService.class);

    /** Nominatim usage policy: at most one request per second. */
    private static final long MIN_INTERVAL_MS = 1100L;
    private static final double EARTH_RADIUS_KM = 6371.0;

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final boolean enabled;
    private final String provider;
    private final double baseLatitude;
    private final double baseLongitude;
    private final double maxDistanceKm;

    /** Cache keyed by normalised address, so each address is resolved at most once per run. */
    private final Map<String, Optional<double[]>> cache = new ConcurrentHashMap<>();
    private final Object rateLimitLock = new Object();
    private long lastRequestAt = 0L;

    public GeocodingService(
            ObjectMapper objectMapper,
            @Value("${bitehub.geocoding.enabled:true}") boolean enabled,
            @Value("${bitehub.geocoding.provider:photon}") String provider,
            @Value("${bitehub.geocoding.user-agent:BiteHub-Demo/1.0}") String userAgent,
            @Value("${bitehub.geocoding.base-latitude:23.7937}") double baseLatitude,
            @Value("${bitehub.geocoding.base-longitude:90.4066}") double baseLongitude,
            @Value("${bitehub.geocoding.max-distance-km:25}") double maxDistanceKm) {
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.provider = provider == null ? "photon" : provider.trim().toLowerCase();
        this.baseLatitude = baseLatitude;
        this.baseLongitude = baseLongitude;
        this.maxDistanceKm = maxDistanceKm;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(3000);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader("User-Agent", userAgent)
                .defaultHeader("Accept", "application/json")
                .build();

        log.info("GeocodingService initialised (enabled={}, provider={}, base=[{}, {}], maxDistanceKm={})",
                enabled, this.provider, baseLatitude, baseLongitude, maxDistanceKm);
    }

    /**
     * @return coordinates as {@code [latitude, longitude]}, or empty when the
     *         address is blank, geocoding is disabled, the provider fails, or
     *         the result is implausibly far from the configured base point.
     */
    public Optional<double[]> geocode(String address) {
        if (!enabled || address == null || address.isBlank()) {
            return Optional.empty();
        }
        String key = address.trim().toLowerCase();
        // computeIfAbsent would hold the bin lock during the network call; use a simple get/put.
        Optional<double[]> cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        Optional<double[]> resolved = lookup(address.trim());
        cache.put(key, resolved);
        return resolved;
    }

    private Optional<double[]> lookup(String address) {
        try {
            rateLimit();
            String body = restClient.get().uri(buildUri(address)).retrieve().body(String.class);
            return parseCoordinates(body);
        } catch (Exception ex) {
            log.warn("Geocoding failed for '{}': {}", address, ex.getMessage());
            return Optional.empty();
        }
    }

    private URI buildUri(String address) {
        if ("nominatim".equals(provider)) {
            return UriComponentsBuilder.fromUriString("https://nominatim.openstreetmap.org/search")
                    .queryParam("q", address)
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .build()
                    .encode()
                    .toUri();
        }
        // Photon (Komoot) is the default: no key, GeoJSON response.
        return UriComponentsBuilder.fromUriString("https://photon.komoot.io/api")
                .queryParam("q", address)
                .queryParam("limit", 1)
                .build()
                .encode()
                .toUri();
    }

    private Optional<double[]> parseCoordinates(String body) {
        if (body == null || body.isBlank()) {
            return Optional.empty();
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            Double latitude = null;
            Double longitude = null;

            if (root.isArray() && root.size() > 0) {
                // Nominatim: [{ "lat": "23.79", "lon": "90.40", ... }]
                JsonNode first = root.get(0);
                latitude = parseNullableDouble(first.path("lat").asText(null));
                longitude = parseNullableDouble(first.path("lon").asText(null));
            } else if (root.has("features")) {
                // Photon: { "features": [ { "geometry": { "coordinates": [lon, lat] } } ] }
                JsonNode features = root.path("features");
                if (features.isArray() && features.size() > 0) {
                    JsonNode coordinates = features.get(0).path("geometry").path("coordinates");
                    if (coordinates.isArray() && coordinates.size() >= 2) {
                        longitude = coordinates.get(0).asDouble();
                        latitude = coordinates.get(1).asDouble();
                    }
                }
            }

            if (latitude == null || longitude == null) {
                return Optional.empty();
            }
            if (distanceKm(baseLatitude, baseLongitude, latitude, longitude) > maxDistanceKm) {
                log.info("Ignoring geocoding hit for an address outside the {} km demo radius", maxDistanceKm);
                return Optional.empty();
            }
            return Optional.of(new double[]{latitude, longitude});
        } catch (Exception ex) {
            log.warn("Could not parse geocoding response: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private void rateLimit() {
        synchronized (rateLimitLock) {
            long wait = MIN_INTERVAL_MS - (System.currentTimeMillis() - lastRequestAt);
            if (wait > 0) {
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            lastRequestAt = System.currentTimeMillis();
        }
    }

    private static Double parseNullableDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.min(1.0, Math.sqrt(a)));
    }
}
