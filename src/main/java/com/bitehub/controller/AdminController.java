package com.bitehub.controller;

import com.bitehub.dto.DashboardStatsDTO;
import com.bitehub.dto.OrderResponseDTO;
import com.bitehub.dto.RestaurantDTO;
import com.bitehub.dto.UserDTO;
import com.bitehub.dto.RefundRequest;
import com.bitehub.service.OrderService;
import com.bitehub.service.ReportService;
import com.bitehub.service.RestaurantService;
import com.bitehub.service.UserService;
import com.bitehub.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final RestaurantService restaurantService;
    private final OrderService orderService;
    private final ReportService reportService;

    public AdminController(UserService userService,
                           RestaurantService restaurantService,
                           OrderService orderService,
                           ReportService reportService) {
        this.userService = userService;
        this.restaurantService = restaurantService;
        this.orderService = orderService;
        this.reportService = reportService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(reportService.getDashboardStats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/create-admin")
    public ResponseEntity<UserDTO> createAdmin(@Valid @RequestBody RegisterRequest request) {
        return new ResponseEntity<>(userService.createAdminUser(request), HttpStatus.CREATED);
    }

    @PatchMapping("/users/{id}/promote-admin")
    public ResponseEntity<UserDTO> promoteToAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(userService.promoteUserToAdmin(id));
    }

    @PatchMapping("/users/{id}/toggle-status")
    public ResponseEntity<UserDTO> toggleUserStatus(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleUserStatus(id));
    }

    @GetMapping("/restaurants")
    public ResponseEntity<List<RestaurantDTO>> getAllRestaurants() {
        return ResponseEntity.ok(restaurantService.getAllRestaurantsForAdmin());
    }

    @GetMapping("/restaurants/pending")
    public ResponseEntity<List<RestaurantDTO>> getPendingRestaurants() {
        return ResponseEntity.ok(restaurantService.getPendingVerificationRestaurants());
    }

    @PutMapping("/restaurants/{id}/verify")
    public ResponseEntity<RestaurantDTO> verifyRestaurant(
            @PathVariable Long id,
            @RequestParam boolean approve) {
        return ResponseEntity.ok(restaurantService.verifyRestaurant(id, approve));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PostMapping("/orders/{id}/refund")
    public ResponseEntity<OrderResponseDTO> issueRefund(
            @PathVariable Long id,
            @Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(orderService.issueRefund(id, request));
    }
}
