/**
 * BiteHub - Shared Leaflet helpers for live order tracking.
 *
 * Loaded after Leaflet. Provides marker icons, tile layers, coordinate
 * resolution (with a simulated fallback when geocoding has not resolved an
 * address) and small geo utilities. Exposed as window.BiteHubMap.
 */
(function () {
    // Fallback coordinates used when an order/restaurant has no stored location.
    const CAMPUS = [23.7937, 90.4066];
    const FALLBACK_CUSTOMER = [23.8115, 90.4244];

    function hasCoords(lat, lng) {
        return typeof lat === 'number' && typeof lng === 'number'
            && Number.isFinite(lat) && Number.isFinite(lng);
    }

    function makeIcon(color, iconClass, size) {
        const html = `
            <div style="background:${color};color:#fff;width:${size}px;height:${size}px;border-radius:50%;
                        display:flex;align-items:center;justify-content:center;
                        box-shadow:0 3px 10px rgba(0,0,0,0.35);border:3px solid #fff;">
                <i class="bi ${iconClass}" style="font-size:${Math.round(size * 0.5)}px;"></i>
            </div>`;
        return L.divIcon({
            className: 'bh-pin',
            html,
            iconSize: [size, size],
            iconAnchor: [size / 2, size / 2],
            popupAnchor: [0, -size / 2]
        });
    }

    const ICON_DEFS = {
        restaurant: { color: '#EF4444', icon: 'bi-shop', size: 34 },
        customer: { color: '#10B981', icon: 'bi-house-door-fill', size: 34 },
        rider: { color: '#3B82F6', icon: 'bi-bicycle', size: 40 }
    };

    const BiteHubMap = {
        CAMPUS,
        FALLBACK_CUSTOMER,

        /** Build a themed marker icon for 'restaurant' | 'rider' | 'customer'. */
        icon(type) {
            const def = ICON_DEFS[type] || ICON_DEFS.rider;
            return makeIcon(def.color, def.icon, def.size);
        },

        /** OpenStreetMap tile layer (keyless). */
        tiles() {
            return L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19,
                attribution: '&copy; <a href="https://openstreetmap.org">OpenStreetMap</a> contributors'
            });
        },

        /**
         * Resolve an order's restaurant and customer positions.
         * Uses geocoded coordinates when present, otherwise sensible campus defaults.
         * @returns {{restaurant: number[], customer: number[], restaurantGeocoded: boolean, customerGeocoded: boolean}}
         */
        resolve(order) {
            const restaurantGeocoded = hasCoords(order.restaurantLatitude, order.restaurantLongitude);
            const customerGeocoded = hasCoords(order.deliveryLatitude, order.deliveryLongitude);
            return {
                restaurant: restaurantGeocoded ? [order.restaurantLatitude, order.restaurantLongitude] : CAMPUS.slice(),
                customer: customerGeocoded ? [order.deliveryLatitude, order.deliveryLongitude] : FALLBACK_CUSTOMER.slice(),
                restaurantGeocoded,
                customerGeocoded
            };
        },

        /** A gently curved polyline between two points, so routes read like roads. */
        route(a, b, steps = 24) {
            const dLat = b[0] - a[0];
            const dLng = b[1] - a[1];
            const len = Math.hypot(dLat, dLng) || 1e-9;
            const perpLat = -dLng / len;
            const perpLng = dLat / len;
            const bow = Math.min(len * 0.12, 0.004);
            const points = [];
            for (let i = 0; i <= steps; i++) {
                const t = i / steps;
                const arc = Math.sin(Math.PI * t) * bow;
                points.push([a[0] + dLat * t + perpLat * arc, a[1] + dLng * t + perpLng * arc]);
            }
            return points;
        },

        /** Offset a [lat, lng] point so overlapping markers stay visible. */
        offset(point, dLat, dLng) {
            return [point[0] + dLat, point[1] + dLng];
        },

        distanceKm(a, b) {
            const R = 6371;
            const dLat = (b[0] - a[0]) * Math.PI / 180;
            const dLng = (b[1] - a[1]) * Math.PI / 180;
            const lat1 = a[0] * Math.PI / 180;
            const lat2 = b[0] * Math.PI / 180;
            const h = Math.sin(dLat / 2) ** 2 + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLng / 2) ** 2;
            return 2 * R * Math.asin(Math.min(1, Math.sqrt(h)));
        },

        formatDistance(km) {
            if (!Number.isFinite(km)) return '';
            return km < 1 ? `${Math.round(km * 1000)} m` : `${km.toFixed(1)} km`;
        }
    };

    window.BiteHubMap = BiteHubMap;
})();
