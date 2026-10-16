package com.bitehub.dto;

import java.time.LocalDateTime;

public class DeliveryLocationDTO {
    private Double latitude;
    private Double longitude;
    private LocalDateTime updatedAt;

    public DeliveryLocationDTO() {}

    public DeliveryLocationDTO(Double latitude, Double longitude, LocalDateTime updatedAt) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.updatedAt = updatedAt;
    }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
