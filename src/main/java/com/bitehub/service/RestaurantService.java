package com.bitehub.service;

import com.bitehub.dto.RestaurantDTO;
import com.bitehub.dto.RestaurantRequest;
import com.bitehub.exception.BadRequestException;
import com.bitehub.exception.ResourceNotFoundException;
import com.bitehub.model.Restaurant;
import com.bitehub.model.User;
import com.bitehub.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final AuthService authService;
    private final GeocodingService geocodingService;

    public RestaurantService(RestaurantRepository restaurantRepository, AuthService authService,
                             GeocodingService geocodingService) {
        this.restaurantRepository = restaurantRepository;
        this.authService = authService;
        this.geocodingService = geocodingService;
    }

    public List<RestaurantDTO> getAllVerifiedRestaurants(String query, String cuisine) {
        List<Restaurant> list;
        if (query != null && !query.trim().isEmpty()) {
            list = restaurantRepository.searchVerifiedRestaurants(query.trim());
        } else if (cuisine != null && !cuisine.trim().isEmpty() && !cuisine.equalsIgnoreCase("all")) {
            list = restaurantRepository.findByCuisineTypeIgnoreCaseAndIsVerifiedTrue(cuisine.trim());
        } else {
            list = restaurantRepository.findByIsVerifiedTrue();
        }
        return list.stream().map(RestaurantDTO::new).collect(Collectors.toList());
    }

    public RestaurantDTO getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));
        return new RestaurantDTO(restaurant);
    }

    public Restaurant getRestaurantEntity(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));
    }

    public RestaurantDTO getMyRestaurant() {
        User user = authService.getCurrentUser();
        Restaurant restaurant = restaurantRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No restaurant profile associated with this account"));
        return new RestaurantDTO(restaurant);
    }

    @Transactional
    public RestaurantDTO updateMyRestaurantProfile(RestaurantRequest request) {
        User user = authService.getCurrentUser();
        Restaurant restaurant = restaurantRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No restaurant profile associated with this account"));

        if (request.getName() != null) restaurant.setName(request.getName());
        if (request.getDescription() != null) restaurant.setDescription(request.getDescription());
        if (request.getCuisineType() != null) restaurant.setCuisineType(request.getCuisineType());
        if (request.getAddress() != null) {
            restaurant.setAddress(request.getAddress());
            geocodingService.geocode(request.getAddress()).ifPresent(coords -> {
                restaurant.setLatitude(coords[0]);
                restaurant.setLongitude(coords[1]);
            });
        }
        if (request.getPhone() != null) restaurant.setPhone(request.getPhone());
        if (request.getImageUrl() != null) restaurant.setImageUrl(request.getImageUrl());
        if (request.getBannerUrl() != null) restaurant.setBannerUrl(request.getBannerUrl());
        if (request.getIsOpen() != null) restaurant.setOpen(request.getIsOpen());

        return new RestaurantDTO(restaurantRepository.save(restaurant));
    }

    @Transactional
    public RestaurantDTO toggleOpenStatus(Long id) {
        Restaurant restaurant = getRestaurantEntity(id);
        
        User user = authService.getCurrentUser();
        if (user.getRole() == com.bitehub.model.Role.ROLE_RESTAURANT) {
            if (!restaurant.getUser().getId().equals(user.getId())) {
                throw new BadRequestException("You do not have permission to toggle status for this restaurant");
            }
        }
        
        restaurant.setOpen(!restaurant.isOpen());
        return new RestaurantDTO(restaurantRepository.save(restaurant));
    }

    // Admin methods
    public List<RestaurantDTO> getPendingVerificationRestaurants() {
        return restaurantRepository.findByIsVerifiedFalse().stream()
                .map(RestaurantDTO::new)
                .collect(Collectors.toList());
    }

    public List<RestaurantDTO> getAllRestaurantsForAdmin() {
        return restaurantRepository.findAll().stream()
                .map(RestaurantDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public RestaurantDTO verifyRestaurant(Long id, boolean approve) {
        Restaurant restaurant = getRestaurantEntity(id);
        restaurant.setVerified(approve);
        return new RestaurantDTO(restaurantRepository.save(restaurant));
    }
}
