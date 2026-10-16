package com.bitehub.dto;

import com.bitehub.model.Coupon;
import com.bitehub.model.DiscountType;
import java.time.LocalDate;

public class CouponDTO {
    private Long id;
    private String code;
    private DiscountType discountType;
    private Double discountValue;
    private Double minOrderAmount;
    private Double maxDiscountAmount;
    private LocalDate expiryDate;
    private boolean isActive;
    private Integer usageLimit;
    private Integer timesUsed;

    public CouponDTO() {}

    public CouponDTO(Coupon c) {
        this.id = c.getId();
        this.code = c.getCode();
        this.discountType = c.getDiscountType();
        this.discountValue = c.getDiscountValue();
        this.minOrderAmount = c.getMinOrderAmount();
        this.maxDiscountAmount = c.getMaxDiscountAmount();
        this.expiryDate = c.getExpiryDate();
        this.isActive = c.isActive();
        this.usageLimit = c.getUsageLimit();
        this.timesUsed = c.getTimesUsed();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }

    public Double getDiscountValue() { return discountValue; }
    public void setDiscountValue(Double discountValue) { this.discountValue = discountValue; }

    public Double getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(Double minOrderAmount) { this.minOrderAmount = minOrderAmount; }

    public Double getMaxDiscountAmount() { return maxDiscountAmount; }
    public void setMaxDiscountAmount(Double maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }

    public Integer getTimesUsed() { return timesUsed; }
    public void setTimesUsed(Integer timesUsed) { this.timesUsed = timesUsed; }
}
