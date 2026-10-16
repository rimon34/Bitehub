package com.bitehub.service;

import com.bitehub.config.JwtTokenProvider;
import com.bitehub.dto.AuthResponse;
import com.bitehub.dto.LoginRequest;
import com.bitehub.dto.RegisterRequest;
import com.bitehub.exception.BadRequestException;
import com.bitehub.model.DeliveryPersonnel;
import com.bitehub.model.Restaurant;
import com.bitehub.model.Role;
import com.bitehub.model.User;
import com.bitehub.repository.DeliveryPersonnelRepository;
import com.bitehub.repository.RestaurantRepository;
import com.bitehub.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final DeliveryPersonnelRepository deliveryPersonnelRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final GeocodingService geocodingService;

    public AuthService(UserRepository userRepository,
                       RestaurantRepository restaurantRepository,
                       DeliveryPersonnelRepository deliveryPersonnelRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       GeocodingService geocodingService) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.deliveryPersonnelRepository = deliveryPersonnelRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.geocodingService = geocodingService;
    }

    @org.springframework.beans.factory.annotation.Value("${admin.secret}")
    private String adminSecret;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        if (request.getRole() == Role.ROLE_ADMIN) {
            if (request.getAdminSecretKey() == null || !request.getAdminSecretKey().trim().equals(adminSecret)) {
                throw new BadRequestException("Invalid Administrator Secret Key.");
            }
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        Long restaurantId = null;
        Long deliveryId = null;

        if (request.getRole() == Role.ROLE_RESTAURANT) {
            Restaurant restaurant = new Restaurant();
            restaurant.setUser(savedUser);
            restaurant.setName(request.getRestaurantName() != null && !request.getRestaurantName().isBlank()
                    ? request.getRestaurantName() : request.getName() + "'s Restaurant");
            restaurant.setCuisineType(request.getCuisineType() != null ? request.getCuisineType() : "Fast Food");
            String restaurantAddress = request.getRestaurantAddress() != null ? request.getRestaurantAddress() : "City Center";
            restaurant.setAddress(restaurantAddress);
            geocodingService.geocode(restaurantAddress).ifPresent(coords -> {
                restaurant.setLatitude(coords[0]);
                restaurant.setLongitude(coords[1]);
            });
            restaurant.setPhone(request.getRestaurantPhone() != null ? request.getRestaurantPhone() : request.getPhone());
            restaurant.setDescription(request.getRestaurantDescription() != null ? request.getRestaurantDescription() : "Delicious food made fresh daily.");
            restaurant.setImageUrl("https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=600&auto=format&fit=crop");
            restaurant.setBannerUrl("https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=1200&auto=format&fit=crop");
            restaurant.setOpen(true);
            restaurant.setVerified(false); // Pending admin verification
            Restaurant savedRestaurant = restaurantRepository.save(restaurant);
            restaurantId = savedRestaurant.getId();
        } else if (request.getRole() == Role.ROLE_DELIVERY) {
            DeliveryPersonnel delivery = new DeliveryPersonnel();
            delivery.setUser(savedUser);
            delivery.setVehicleType(request.getVehicleType() != null ? request.getVehicleType() : "Motorcycle");
            delivery.setLicenseNumber(request.getLicenseNumber() != null ? request.getLicenseNumber() : "DL-" + System.currentTimeMillis());
            delivery.setAvailable(true);
            DeliveryPersonnel savedDelivery = deliveryPersonnelRepository.save(delivery);
            deliveryId = savedDelivery.getId();
        }

        String token = tokenProvider.generateToken(savedUser.getEmail(), savedUser.getRole().name());
        AuthResponse response = new AuthResponse(token, savedUser.getId(), savedUser.getName(), savedUser.getEmail(), savedUser.getRole());
        response.setProfileImageUrl(savedUser.getProfileImageUrl());
        response.setRestaurantId(restaurantId);
        response.setDeliveryPersonnelId(deliveryId);
        return response;
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase().trim(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!user.isEnabled()) {
            throw new BadRequestException("Account has been disabled by Administrator");
        }

        String token = tokenProvider.generateToken(user.getEmail(), user.getRole().name());
        AuthResponse response = new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
        response.setProfileImageUrl(user.getProfileImageUrl());

        if (user.getRole() == Role.ROLE_RESTAURANT) {
            restaurantRepository.findByUserId(user.getId()).ifPresent(r -> response.setRestaurantId(r.getId()));
        } else if (user.getRole() == Role.ROLE_DELIVERY) {
            deliveryPersonnelRepository.findByUserId(user.getId()).ifPresent(d -> response.setDeliveryPersonnelId(d.getId()));
        }

        return response;
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BadRequestException("Not authenticated");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new BadRequestException("Current user not found"));
    }
}
