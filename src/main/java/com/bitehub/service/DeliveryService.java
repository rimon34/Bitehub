package com.bitehub.service;

import com.bitehub.dto.DeliveryLocationDTO;
import com.bitehub.dto.DeliveryLocationUpdateRequest;
import com.bitehub.dto.OrderResponseDTO;
import com.bitehub.exception.BadRequestException;
import com.bitehub.exception.ResourceNotFoundException;
import com.bitehub.model.*;
import com.bitehub.repository.DeliveryPersonnelRepository;
import com.bitehub.repository.DeliveryRepository;
import com.bitehub.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryPersonnelRepository deliveryPersonnelRepository;
    private final OrderRepository orderRepository;
    private final AuthService authService;
    private final OrderEventService orderEventService;

    public DeliveryService(DeliveryRepository deliveryRepository,
                           DeliveryPersonnelRepository deliveryPersonnelRepository,
                           OrderRepository orderRepository,
                           AuthService authService,
                           OrderEventService orderEventService) {
        this.deliveryRepository = deliveryRepository;
        this.deliveryPersonnelRepository = deliveryPersonnelRepository;
        this.orderRepository = orderRepository;
        this.authService = authService;
        this.orderEventService = orderEventService;
    }

    public List<OrderResponseDTO> getMyAssignedOrders() {
        User user = authService.getCurrentUser();
        DeliveryPersonnel dp = deliveryPersonnelRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No delivery personnel profile associated with this account"));

        return orderRepository.findByDeliveryPersonnelIdOrderByPlacedAtDesc(dp.getId()).stream()
                .map(OrderResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getAvailableDeliveryPersonnel() {
        return deliveryPersonnelRepository.findAll().stream()
                .map(dp -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", dp.getId());
                    map.put("name", dp.getUser().getName());
                    map.put("phone", dp.getUser().getPhone());
                    map.put("vehicleType", dp.getVehicleType());
                    map.put("isAvailable", dp.isAvailable());
                    map.put("activeOrdersCount", dp.getActiveOrdersCount());
                    map.put("rating", dp.getRating());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponseDTO updateDeliveryStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        User user = authService.getCurrentUser();
        if (user.getRole() == Role.ROLE_DELIVERY) {
            if (order.getDeliveryPersonnel() == null || !order.getDeliveryPersonnel().getUser().getId().equals(user.getId())) {
                throw new BadRequestException("You are not assigned to this order");
            }
        }

        if (newStatus != OrderStatus.OUT_FOR_DELIVERY && newStatus != OrderStatus.DELIVERED) {
            throw new BadRequestException("Delivery personnel can only transition orders to OUT_FOR_DELIVERY or DELIVERED");
        }

        if (newStatus == OrderStatus.OUT_FOR_DELIVERY && order.getOrderStatus() != OrderStatus.READY_FOR_PICKUP) {
            throw new BadRequestException("Only ready-for-pickup orders can be dispatched");
        }
        if (newStatus == OrderStatus.DELIVERED && order.getOrderStatus() != OrderStatus.OUT_FOR_DELIVERY) {
            throw new BadRequestException("Only dispatched orders can be delivered");
        }

        order.setOrderStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        Delivery delivery = deliveryRepository.findByOrderId(order.getId()).orElse(null);
        if (delivery != null) {
            delivery.setDeliveryStatus(newStatus);
            if (newStatus == OrderStatus.OUT_FOR_DELIVERY && delivery.getPickedUpAt() == null) {
                delivery.setPickedUpAt(LocalDateTime.now());
            } else if (newStatus == OrderStatus.DELIVERED) {
                delivery.setDeliveredAt(LocalDateTime.now());
                order.setPaymentStatus(PaymentStatus.COMPLETED);

                DeliveryPersonnel dp = delivery.getDeliveryPersonnel();
                if (dp != null && dp.getActiveOrdersCount() > 0) {
                    dp.setActiveOrdersCount(dp.getActiveOrdersCount() - 1);
                    deliveryPersonnelRepository.save(dp);
                }
            }
            deliveryRepository.save(delivery);
        } else if (newStatus == OrderStatus.DELIVERED) {
            order.setPaymentStatus(PaymentStatus.COMPLETED);
        }

        Order savedOrder = orderRepository.save(order);
        orderEventService.publish(savedOrder);
        return new OrderResponseDTO(savedOrder);
    }

    @Transactional
    public DeliveryLocationDTO updateMyLocation(Long orderId, DeliveryLocationUpdateRequest request) {
        User user = authService.getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        if (user.getRole() != Role.ROLE_DELIVERY || order.getDeliveryPersonnel() == null ||
                !order.getDeliveryPersonnel().getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You are not assigned to this delivery");
        }
        if (order.getOrderStatus() != OrderStatus.OUT_FOR_DELIVERY) {
            throw new BadRequestException("Location can only be shared while an order is out for delivery");
        }

        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery record not found"));
        delivery.setCurrentLatitude(request.getLatitude());
        delivery.setCurrentLongitude(request.getLongitude());
        delivery.setLocationUpdatedAt(LocalDateTime.now());
        deliveryRepository.save(delivery);
        orderEventService.publish(order);
        return new DeliveryLocationDTO(delivery.getCurrentLatitude(), delivery.getCurrentLongitude(), delivery.getLocationUpdatedAt());
    }

    public DeliveryLocationDTO getOrderLocation(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        User user = authService.getCurrentUser();
        boolean allowed = user.getRole() == Role.ROLE_ADMIN ||
                (user.getRole() == Role.ROLE_CUSTOMER && order.getCustomer().getId().equals(user.getId())) ||
                (user.getRole() == Role.ROLE_RESTAURANT && order.getRestaurant().getUser().getId().equals(user.getId())) ||
                (user.getRole() == Role.ROLE_DELIVERY && order.getDeliveryPersonnel() != null &&
                        order.getDeliveryPersonnel().getUser().getId().equals(user.getId()));
        if (!allowed) {
            throw new BadRequestException("You do not have permission to view this location");
        }
        Delivery delivery = deliveryRepository.findByOrderId(orderId).orElse(null);
        if (delivery == null) {
            return new DeliveryLocationDTO(null, null, null);
        }
        return new DeliveryLocationDTO(delivery.getCurrentLatitude(), delivery.getCurrentLongitude(), delivery.getLocationUpdatedAt());
    }
}
