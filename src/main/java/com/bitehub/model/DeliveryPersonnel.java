package com.bitehub.model;

import jakarta.persistence.*;

@Entity
@Table(name = "delivery_personnel")
public class DeliveryPersonnel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String vehicleType; // Bike, Motorcycle, Scooter, Car
    private String licenseNumber;

    @Column(nullable = false)
    private boolean isAvailable = true;

    private Integer activeOrdersCount = 0;
    private Double rating = 4.8;

    public DeliveryPersonnel() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public Integer getActiveOrdersCount() { return activeOrdersCount; }
    public void setActiveOrdersCount(Integer activeOrdersCount) { this.activeOrdersCount = activeOrdersCount; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
}
