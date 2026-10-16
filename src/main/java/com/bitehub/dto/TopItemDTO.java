package com.bitehub.dto;

public class TopItemDTO {
    private String itemName;
    private long totalQuantity;
    private double totalRevenue;

    public TopItemDTO() {}

    public TopItemDTO(String itemName, long totalQuantity, double totalRevenue) {
        this.itemName = itemName;
        this.totalQuantity = totalQuantity;
        this.totalRevenue = totalRevenue;
    }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public long getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(long totalQuantity) { this.totalQuantity = totalQuantity; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
}
