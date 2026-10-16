package com.bitehub.dto;

import jakarta.validation.constraints.NotNull;

public class AssignDeliveryRequest {

    @NotNull(message = "Delivery personnel ID is required")
    private Long deliveryPersonnelId;

    public AssignDeliveryRequest() {}

    public AssignDeliveryRequest(Long deliveryPersonnelId) {
        this.deliveryPersonnelId = deliveryPersonnelId;
    }

    public Long getDeliveryPersonnelId() { return deliveryPersonnelId; }
    public void setDeliveryPersonnelId(Long deliveryPersonnelId) { this.deliveryPersonnelId = deliveryPersonnelId; }
}
