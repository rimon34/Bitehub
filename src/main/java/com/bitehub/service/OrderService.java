package com.bitehub.service;

import com.bitehub.dto.*;
import com.bitehub.exception.BadRequestException;
import com.bitehub.exception.ResourceNotFoundException;
import com.bitehub.model.*;
import com.bitehub.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final DeliveryPersonnelRepository deliveryPersonnelRepository;
    private final PaymentRepository paymentRepository;
    private final DeliveryRepository deliveryRepository;
    private final CouponService couponService;
    private final AuthService authService;
    private final OrderEventService orderEventService;
    private final GeocodingService geocodingService;

    @Value("${orders.customer-cancellation-window-minutes:10}")
    private long customerCancellationWindowMinutes;

    public OrderService(OrderRepository orderRepository,
                        MenuItemRepository menuItemRepository,
                        RestaurantRepository restaurantRepository,
                        DeliveryPersonnelRepository deliveryPersonnelRepository,
                        PaymentRepository paymentRepository,
                        DeliveryRepository deliveryRepository,
                        CouponService couponService,
                        AuthService authService,
                        OrderEventService orderEventService,
                        GeocodingService geocodingService) {
        this.orderRepository = orderRepository;
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
        this.deliveryPersonnelRepository = deliveryPersonnelRepository;
        this.paymentRepository = paymentRepository;
        this.deliveryRepository = deliveryRepository;
        this.couponService = couponService;
        this.authService = authService;
        this.orderEventService = orderEventService;
        this.geocodingService = geocodingService;
    }

    @Transactional
    public OrderResponseDTO createOrder(OrderRequest request) {
        User customer = authService.getCurrentUser();

        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + request.getRestaurantId()));

        if (!restaurant.isOpen()) {
            throw new BadRequestException("Restaurant is currently closed");
        }

        if (!restaurant.isVerified()) {
            throw new BadRequestException("Restaurant is pending administrator verification");
        }

        Order order = new Order();
        order.setOrderNumber("BH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setDeliveryAddressText(request.getDeliveryAddress());
        applyDeliveryCoordinates(order);
        ensureRestaurantCoordinates(restaurant);
        order.setCustomerPhone(request.getCustomerPhone() != null ? request.getCustomerPhone() : customer.getPhone());
        order.setSpecialInstructions(request.getSpecialInstructions());
        order.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.CASH_ON_DELIVERY);
        order.setOrderStatus(OrderStatus.PLACED);
        order.setPlacedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        double subtotal = 0.0;
        for (OrderItemRequest itemReq : request.getItems()) {
            if (itemReq.getQuantity() <= 0) {
                throw new BadRequestException("Item quantity must be greater than zero");
            }
            
            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + itemReq.getMenuItemId()));

            if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
                throw new BadRequestException("Item " + menuItem.getName() + " does not belong to the selected restaurant");
            }

            if (!menuItem.isAvailable()) {
                throw new BadRequestException("Item " + menuItem.getName() + " is currently sold out");
            }

            double lineTotal = menuItem.getPrice() * itemReq.getQuantity();
            subtotal += lineTotal;

            OrderItem orderItem = new OrderItem(menuItem, menuItem.getName(), menuItem.getPrice(), itemReq.getQuantity(), lineTotal);
            order.addItem(orderItem);
        }

        order.setSubtotal(Math.round(subtotal * 100.0) / 100.0);
        order.setDeliveryFee(40.0); // flat standard delivery fee
        order.setTaxAmount(Math.round(subtotal * 0.05 * 100.0) / 100.0); // 5% tax

        double discount = 0.0;
        if (request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
            var valResp = couponService.validateCoupon(new CouponValidateRequest(request.getCouponCode(), subtotal));
            if (valResp.isValid()) {
                discount = valResp.getDiscountAmount();
                order.setAppliedCouponCode(valResp.getCode());
                couponService.recordUsage(valResp.getCode());
            }
        }
        order.setDiscountAmount(discount);

        double total = order.getSubtotal() + order.getDeliveryFee() + order.getTaxAmount() - discount;
        order.setTotalAmount(Math.max(0.0, Math.round(total * 100.0) / 100.0));

        // Setup simulated payment
        if (order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
            order.setPaymentStatus(PaymentStatus.PENDING);
        } else {
            // Simulated Card / Mobile Banking
            order.setPaymentStatus(PaymentStatus.COMPLETED);
        }

        Order savedOrder = orderRepository.save(order);

        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setPaymentMethod(savedOrder.getPaymentMethod());
        payment.setAmount(savedOrder.getTotalAmount());
        payment.setStatus(savedOrder.getPaymentStatus());
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        return responseAndPublish(savedOrder);
    }

    @Transactional
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        validateOrderAccess(order);
        backfillCoordinates(order);
        return new OrderResponseDTO(order);
    }

    @Transactional
    public OrderResponseDTO getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));
        validateOrderAccess(order);
        backfillCoordinates(order);
        return new OrderResponseDTO(order);
    }

    /** Geocodes the delivery address snapshot onto the order, when resolvable. */
    private void applyDeliveryCoordinates(Order order) {
        if (order.getDeliveryLatitude() != null && order.getDeliveryLongitude() != null) {
            return;
        }
        geocodingService.geocode(order.getDeliveryAddressText()).ifPresent(coords -> {
            order.setDeliveryLatitude(coords[0]);
            order.setDeliveryLongitude(coords[1]);
        });
    }

    /** Resolves and persists a store's coordinates the first time they are needed. */
    private void ensureRestaurantCoordinates(Restaurant restaurant) {
        if (restaurant == null || (restaurant.getLatitude() != null && restaurant.getLongitude() != null)) {
            return;
        }
        geocodingService.geocode(restaurant.getAddress()).ifPresent(coords -> {
            restaurant.setLatitude(coords[0]);
            restaurant.setLongitude(coords[1]);
            restaurantRepository.save(restaurant);
        });
    }

    /**
     * Lazily fills in any missing coordinates the first time an order is served.
     * Covers demo/seeded rows without depending on the network at startup.
     */
    private void backfillCoordinates(Order order) {
        boolean orderChanged = false;
        if (order.getDeliveryLatitude() == null || order.getDeliveryLongitude() == null) {
            if (geocodingService.geocode(order.getDeliveryAddressText()).isPresent()) {
                applyDeliveryCoordinates(order);
                orderChanged = true;
            }
        }
        ensureRestaurantCoordinates(order.getRestaurant());
        if (orderChanged) {
            orderRepository.save(order);
        }
    }

    private void validateOrderAccess(Order order) {
        User user = authService.getCurrentUser();
        if (user.getRole() == Role.ROLE_ADMIN) return;
        
        if (user.getRole() == Role.ROLE_CUSTOMER && !order.getCustomer().getId().equals(user.getId())) {
            throw new BadRequestException("You do not have permission to access this order");
        } else if (user.getRole() == Role.ROLE_RESTAURANT) {
            if (order.getRestaurant() == null || !order.getRestaurant().getUser().getId().equals(user.getId())) {
                throw new BadRequestException("You do not have permission to access this order");
            }
        } else if (user.getRole() == Role.ROLE_DELIVERY) {
            if (order.getDeliveryPersonnel() == null || !order.getDeliveryPersonnel().getUser().getId().equals(user.getId())) {
                throw new BadRequestException("You do not have permission to access this order");
            }
        }
    }

    public List<OrderResponseDTO> getMyOrders() {
        User user = authService.getCurrentUser();
        return orderRepository.findByCustomerIdOrderByPlacedAtDesc(user.getId()).stream()
                .map(OrderResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> getRestaurantOrders(Long restaurantId) {
        User user = authService.getCurrentUser();
        if (user.getRole() == Role.ROLE_RESTAURANT) {
            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
            if (!restaurant.getUser().getId().equals(user.getId())) {
                throw new BadRequestException("You do not have permission to view orders for this restaurant");
            }
        }
        return orderRepository.findByRestaurantIdOrderByPlacedAtDesc(restaurantId).stream()
                .map(OrderResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAllByOrderByPlacedAtDesc().stream()
                .map(OrderResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponseDTO updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        User user = authService.getCurrentUser();
        if (user.getRole() == Role.ROLE_RESTAURANT) {
            if (order.getRestaurant() == null || !order.getRestaurant().getUser().getId().equals(user.getId())) {
                throw new BadRequestException("You do not have permission to update this order");
            }
        }

        OrderStatus current = order.getOrderStatus();
        OrderStatus target = request.getStatus();

        // Enforce valid transitions
        if (current == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cancelled orders cannot be updated");
        }
        if (current == OrderStatus.DELIVERED) {
            throw new BadRequestException("Delivered orders cannot be updated");
        }

        if (target == OrderStatus.CANCELLED) {
            return cancelOrderInternal(order, request.getReason());
        }

        if (user.getRole() != Role.ROLE_ADMIN && !isValidStatusTransition(current, target)) {
            throw new BadRequestException("Invalid status transition from " + current + " to " + target);
        }

        order.setOrderStatus(target);
        order.setUpdatedAt(LocalDateTime.now());

        if (target == OrderStatus.DELIVERED) {
            order.setPaymentStatus(PaymentStatus.COMPLETED);
            // update delivery record if present
            deliveryRepository.findByOrderId(order.getId()).ifPresent(d -> {
                d.setDeliveryStatus(OrderStatus.DELIVERED);
                d.setDeliveredAt(LocalDateTime.now());
                deliveryRepository.save(d);

                DeliveryPersonnel dp = d.getDeliveryPersonnel();
                if (dp != null && dp.getActiveOrdersCount() > 0) {
                    dp.setActiveOrdersCount(dp.getActiveOrdersCount() - 1);
                    deliveryPersonnelRepository.save(dp);
                }
            });
        }

        return responseAndPublish(orderRepository.save(order));
    }

    /** Restricts status changes to the forward-only flow defined by the order event matrix. */
    private boolean isValidStatusTransition(OrderStatus current, OrderStatus target) {
        switch (current) {
            case PLACED:
                return target == OrderStatus.CONFIRMED;
            case CONFIRMED:
                return target == OrderStatus.PREPARING;
            case PREPARING:
                return target == OrderStatus.READY_FOR_PICKUP;
            case READY_FOR_PICKUP:
                return target == OrderStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY:
                return target == OrderStatus.DELIVERED;
            default:
                return false;
        }
    }

    @Transactional
    public OrderResponseDTO cancelOrder(Long orderId, CancelOrderRequest request) {
        User user = authService.getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Ensure users can only cancel orders they are authorized to access
        if (user.getRole() == Role.ROLE_CUSTOMER) {
            if (!order.getCustomer().getId().equals(user.getId())) {
                throw new BadRequestException("You do not have permission to cancel this order");
            }
            if (order.getOrderStatus() != OrderStatus.PLACED && order.getOrderStatus() != OrderStatus.CONFIRMED) {
                throw new BadRequestException("Order cannot be cancelled once cooking or delivery is in progress (Current status: " + order.getOrderStatus() + ")");
            }
            if (LocalDateTime.now().isAfter(order.getPlacedAt().plusMinutes(customerCancellationWindowMinutes))) {
                throw new BadRequestException("The " + customerCancellationWindowMinutes + " minute cancellation window has expired");
            }
        } else if (user.getRole() == Role.ROLE_RESTAURANT) {
            if (order.getRestaurant() == null || !order.getRestaurant().getUser().getId().equals(user.getId())) {
                throw new BadRequestException("You do not have permission to cancel this order");
            }
        } else if (user.getRole() == Role.ROLE_DELIVERY) {
            if (order.getDeliveryPersonnel() == null || !order.getDeliveryPersonnel().getUser().getId().equals(user.getId())) {
                throw new BadRequestException("You do not have permission to cancel this order");
            }
        }

        return cancelOrderInternal(order, request.getReason());
    }

    private OrderResponseDTO cancelOrderInternal(Order order, String reason) {
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancellationReason(reason != null ? reason : "Cancelled by user");
        order.setUpdatedAt(LocalDateTime.now());

        // Release delivery agent if assigned
        deliveryRepository.findByOrderId(order.getId()).ifPresent(d -> {
            d.setDeliveryStatus(OrderStatus.CANCELLED);
            deliveryRepository.save(d);

            DeliveryPersonnel dp = d.getDeliveryPersonnel();
            if (dp != null && dp.getActiveOrdersCount() > 0) {
                dp.setActiveOrdersCount(dp.getActiveOrdersCount() - 1);
                deliveryPersonnelRepository.save(dp);
            }
        });

        return responseAndPublish(orderRepository.save(order));
    }

    @Transactional
    public OrderResponseDTO issueRefund(Long orderId, RefundRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        if (order.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new BadRequestException("Only completed payments can be refunded");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found"));
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new BadRequestException("This payment has already been refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundReason(request.getReason().trim());
        payment.setRefundedAt(LocalDateTime.now());
        paymentRepository.save(payment);
        order.setPaymentStatus(PaymentStatus.REFUNDED);
        order.setUpdatedAt(LocalDateTime.now());
        return responseAndPublish(orderRepository.save(order));
    }

    @Transactional
    public OrderResponseDTO assignDeliveryPersonnel(Long orderId, AssignDeliveryRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        User user = authService.getCurrentUser();
        if (user.getRole() == Role.ROLE_RESTAURANT) {
            if (order.getRestaurant() == null || !order.getRestaurant().getUser().getId().equals(user.getId())) {
                throw new BadRequestException("You do not have permission to assign delivery for this order");
            }
        }

        if (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot assign a delivery rider to a " + order.getOrderStatus() + " order");
        }

        DeliveryPersonnel deliveryPersonnel = deliveryPersonnelRepository.findById(request.getDeliveryPersonnelId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery personnel not found with id: " + request.getDeliveryPersonnelId()));

        if (order.getDeliveryPersonnel() != null && !order.getDeliveryPersonnel().getId().equals(deliveryPersonnel.getId())) {
            DeliveryPersonnel oldDp = order.getDeliveryPersonnel();
            if (oldDp.getActiveOrdersCount() > 0) {
                oldDp.setActiveOrdersCount(oldDp.getActiveOrdersCount() - 1);
                deliveryPersonnelRepository.save(oldDp);
            }
        }

        order.setDeliveryPersonnel(deliveryPersonnel);
        order.setOrderStatus(OrderStatus.READY_FOR_PICKUP);
        order.setUpdatedAt(LocalDateTime.now());

        Delivery delivery = deliveryRepository.findByOrderId(order.getId()).orElse(new Delivery());
        delivery.setOrder(order);
        delivery.setDeliveryPersonnel(deliveryPersonnel);
        delivery.setDeliveryStatus(OrderStatus.READY_FOR_PICKUP);
        delivery.setAssignedAt(LocalDateTime.now());
        deliveryRepository.save(delivery);

        deliveryPersonnel.setActiveOrdersCount(deliveryPersonnel.getActiveOrdersCount() + 1);
        deliveryPersonnelRepository.save(deliveryPersonnel);

        return responseAndPublish(orderRepository.save(order));
    }

    private OrderResponseDTO responseAndPublish(Order order) {
        OrderResponseDTO response = new OrderResponseDTO(order);
        orderEventService.publish(order);
        return response;
    }
}
