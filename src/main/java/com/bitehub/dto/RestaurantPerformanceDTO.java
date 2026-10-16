package com.bitehub.dto;

public class RestaurantPerformanceDTO {
    private Long restaurantId;
    private String restaurantName;
    private String cuisineType;
    private long totalOrders;
    private long completedOrders;
    private long cancelledOrders;
    private double totalRevenue;
    private double rating;

    public RestaurantPerformanceDTO() {}

    public RestaurantPerformanceDTO(Long restaurantId, String restaurantName, String cuisineType, long totalOrders, long completedOrders, long cancelledOrders, double totalRevenue, double rating) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.cuisineType = cuisineType;
        this.totalOrders = totalOrders;
        this.completedOrders = completedOrders;
        this.cancelledOrders = cancelledOrders;
        this.totalRevenue = totalRevenue;
        this.rating = rating;
    }

    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }

    public String getCuisineType() { return cuisineType; }
    public void setCuisineType(String cuisineType) { this.cuisineType = cuisineType; }

    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

    public long getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(long completedOrders) { this.completedOrders = completedOrders; }

    public long getCancelledOrders() { return cancelledOrders; }
    public void setCancelledOrders(long cancelledOrders) { this.cancelledOrders = cancelledOrders; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
}
