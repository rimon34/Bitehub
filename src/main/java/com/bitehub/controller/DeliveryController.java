package com.bitehub.controller;

import com.bitehub.dto.DeliveryLocationDTO;
import com.bitehub.dto.DeliveryLocationUpdateRequest;
import com.bitehub.dto.OrderResponseDTO;
import jakarta.validation.Valid;
import com.bitehub.model.OrderStatus;
import com.bitehub.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping("/my-deliveries")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<List<OrderResponseDTO>> getMyDeliveries() {
        return ResponseEntity.ok(deliveryService.getMyAssignedOrders());
    }

    @GetMapping("/available-riders")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAvailableRiders() {
        return ResponseEntity.ok(deliveryService.getAvailableDeliveryPersonnel());
    }

    @PutMapping("/orders/{orderId}/status")
    @PreAuthorize("hasAnyRole('DELIVERY', 'ADMIN')")
    public ResponseEntity<OrderResponseDTO> updateDeliveryStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(deliveryService.updateDeliveryStatus(orderId, status));
    }

    @PutMapping("/orders/{orderId}/location")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<DeliveryLocationDTO> updateLocation(
            @PathVariable Long orderId,
            @Valid @RequestBody DeliveryLocationUpdateRequest request) {
        return ResponseEntity.ok(deliveryService.updateMyLocation(orderId, request));
    }

    @GetMapping("/orders/{orderId}/location")
    public ResponseEntity<DeliveryLocationDTO> getLocation(@PathVariable Long orderId) {
        return ResponseEntity.ok(deliveryService.getOrderLocation(orderId));
    }
}
