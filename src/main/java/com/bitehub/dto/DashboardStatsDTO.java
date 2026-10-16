package com.bitehub.dto;

public class DashboardStatsDTO {
    private long totalUsers;
    private long totalCustomers;
    private long totalRestaurants;
    private long pendingRestaurants;
    private long totalDelivery;
    private long totalOrders;
    private long pendingOrders;
    private long activeOrders;
    private long completedOrders;
    private long cancelledOrders;
    private double totalRevenue;
    private long openComplaints;

    public DashboardStatsDTO() {}

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(long totalCustomers) { this.totalCustomers = totalCustomers; }

    public long getTotalRestaurants() { return totalRestaurants; }
    public void setTotalRestaurants(long totalRestaurants) { this.totalRestaurants = totalRestaurants; }

    public long getPendingRestaurants() { return pendingRestaurants; }
    public void setPendingRestaurants(long pendingRestaurants) { this.pendingRestaurants = pendingRestaurants; }

    public long getTotalDelivery() { return totalDelivery; }
    public void setTotalDelivery(long totalDelivery) { this.totalDelivery = totalDelivery; }

    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

    public long getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; }

    public long getActiveOrders() { return activeOrders; }
    public void setActiveOrders(long activeOrders) { this.activeOrders = activeOrders; }

    public long getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(long completedOrders) { this.completedOrders = completedOrders; }

    public long getCancelledOrders() { return cancelledOrders; }
    public void setCancelledOrders(long cancelledOrders) { this.cancelledOrders = cancelledOrders; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public long getOpenComplaints() { return openComplaints; }
    public void setOpenComplaints(long openComplaints) { this.openComplaints = openComplaints; }
}
