package com.bitehub.controller;

import com.bitehub.dto.RestaurantDTO;
import com.bitehub.dto.RestaurantRequest;
import com.bitehub.service.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping
    public ResponseEntity<List<RestaurantDTO>> getAllRestaurants(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String cuisine) {
        return ResponseEntity.ok(restaurantService.getAllVerifiedRestaurants(query, cuisine));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDTO> getRestaurantById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    @GetMapping("/my-restaurant")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<RestaurantDTO> getMyRestaurant() {
        return ResponseEntity.ok(restaurantService.getMyRestaurant());
    }

    @PutMapping("/my-restaurant")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<RestaurantDTO> updateMyRestaurantProfile(@Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.ok(restaurantService.updateMyRestaurantProfile(request));
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'ADMIN')")
    public ResponseEntity<RestaurantDTO> toggleOpenStatus(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.toggleOpenStatus(id));
    }
}
