package com.bitehub.dto;

import com.bitehub.model.Order;
import com.bitehub.model.OrderStatus;
import com.bitehub.model.PaymentMethod;
import com.bitehub.model.PaymentStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OrderResponseDTO {
    private Long id;
    private String orderNumber;

    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    private Long restaurantId;
    private String restaurantName;
    private String restaurantAddress;
    private String restaurantPhone;
    private Double restaurantLatitude;
    private Double restaurantLongitude;

    private Long deliveryPersonnelId;
    private String deliveryPersonnelName;
    private String deliveryPersonnelPhone;
    private String vehicleType;

    private OrderStatus orderStatus;
    private Double subtotal;
    private Double deliveryFee;
    private Double discountAmount;
    private Double taxAmount;
    private Double totalAmount;

    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;

    private String deliveryAddressText;
    private Double deliveryLatitude;
    private Double deliveryLongitude;
    private String specialInstructions;
    private String cancellationReason;
    private String appliedCouponCode;

    private LocalDateTime placedAt;
    private LocalDateTime updatedAt;

    private List<OrderItemDTO> items = new ArrayList<>();

    public OrderResponseDTO() {}

    public OrderResponseDTO(Order order) {
        this.id = order.getId();
        this.orderNumber = order.getOrderNumber();

        if (order.getCustomer() != null) {
            this.customerId = order.getCustomer().getId();
            this.customerName = order.getCustomer().getName();
            this.customerEmail = order.getCustomer().getEmail();
            this.customerPhone = order.getCustomer().getPhone();
        }

        if (order.getRestaurant() != null) {
            this.restaurantId = order.getRestaurant().getId();
            this.restaurantName = order.getRestaurant().getName();
            this.restaurantAddress = order.getRestaurant().getAddress();
            this.restaurantPhone = order.getRestaurant().getPhone();
            this.restaurantLatitude = order.getRestaurant().getLatitude();
            this.restaurantLongitude = order.getRestaurant().getLongitude();
        }

        if (order.getDeliveryPersonnel() != null) {
            this.deliveryPersonnelId = order.getDeliveryPersonnel().getId();
            if (order.getDeliveryPersonnel().getUser() != null) {
                this.deliveryPersonnelName = order.getDeliveryPersonnel().getUser().getName();
                this.deliveryPersonnelPhone = order.getDeliveryPersonnel().getUser().getPhone();
            }
            this.vehicleType = order.getDeliveryPersonnel().getVehicleType();
        }

        this.orderStatus = order.getOrderStatus();
        this.subtotal = order.getSubtotal();
        this.deliveryFee = order.getDeliveryFee();
        this.discountAmount = order.getDiscountAmount();
        this.taxAmount = order.getTaxAmount();
        this.totalAmount = order.getTotalAmount();

        this.paymentStatus = order.getPaymentStatus();
        this.paymentMethod = order.getPaymentMethod();

        this.deliveryAddressText = order.getDeliveryAddressText();
        this.deliveryLatitude = order.getDeliveryLatitude();
        this.deliveryLongitude = order.getDeliveryLongitude();
        this.specialInstructions = order.getSpecialInstructions();
        this.cancellationReason = order.getCancellationReason();
        this.appliedCouponCode = order.getAppliedCouponCode();

        this.placedAt = order.getPlacedAt();
        this.updatedAt = order.getUpdatedAt();

        if (order.getItems() != null) {
            this.items = order.getItems().stream().map(OrderItemDTO::new).collect(Collectors.toList());
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }

    public String getRestaurantAddress() { return restaurantAddress; }
    public void setRestaurantAddress(String restaurantAddress) { this.restaurantAddress = restaurantAddress; }

    public String getRestaurantPhone() { return restaurantPhone; }
    public void setRestaurantPhone(String restaurantPhone) { this.restaurantPhone = restaurantPhone; }

    public Double getRestaurantLatitude() { return restaurantLatitude; }
    public void setRestaurantLatitude(Double restaurantLatitude) { this.restaurantLatitude = restaurantLatitude; }

    public Double getRestaurantLongitude() { return restaurantLongitude; }
    public void setRestaurantLongitude(Double restaurantLongitude) { this.restaurantLongitude = restaurantLongitude; }

    public Long getDeliveryPersonnelId() { return deliveryPersonnelId; }
    public void setDeliveryPersonnelId(Long deliveryPersonnelId) { this.deliveryPersonnelId = deliveryPersonnelId; }

    public String getDeliveryPersonnelName() { return deliveryPersonnelName; }
    public void setDeliveryPersonnelName(String deliveryPersonnelName) { this.deliveryPersonnelName = deliveryPersonnelName; }

    public String getDeliveryPersonnelPhone() { return deliveryPersonnelPhone; }
    public void setDeliveryPersonnelPhone(String deliveryPersonnelPhone) { this.deliveryPersonnelPhone = deliveryPersonnelPhone; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public OrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }

    public OrderStatus getStatus() { return orderStatus; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public Double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(Double deliveryFee) { this.deliveryFee = deliveryFee; }

    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }

    public Double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(Double taxAmount) { this.taxAmount = taxAmount; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getDeliveryAddressText() { return deliveryAddressText; }
    public void setDeliveryAddressText(String deliveryAddressText) { this.deliveryAddressText = deliveryAddressText; }

    public Double getDeliveryLatitude() { return deliveryLatitude; }
    public void setDeliveryLatitude(Double deliveryLatitude) { this.deliveryLatitude = deliveryLatitude; }

    public Double getDeliveryLongitude() { return deliveryLongitude; }
    public void setDeliveryLongitude(Double deliveryLongitude) { this.deliveryLongitude = deliveryLongitude; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public String getAppliedCouponCode() { return appliedCouponCode; }
    public void setAppliedCouponCode(String appliedCouponCode) { this.appliedCouponCode = appliedCouponCode; }

    public LocalDateTime getPlacedAt() { return placedAt; }
    public void setPlacedAt(LocalDateTime placedAt) { this.placedAt = placedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }
}
