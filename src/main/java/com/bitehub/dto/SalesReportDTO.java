package com.bitehub.dto;

public class SalesReportDTO {
    private String period; // e.g. "2026-09-17" or "Week 38" or "September 2026"
    private long totalOrders;
    private double totalRevenue;

    public SalesReportDTO() {}

    public SalesReportDTO(String period, long totalOrders, double totalRevenue) {
        this.period = period;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
    }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
}
