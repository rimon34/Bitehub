package com.bitehub.dto;

public class CouponValidationResponse {
    private boolean valid;
    private String message;
    private String code;
    private Double discountAmount;
    private Double newTotal;

    public CouponValidationResponse() {}

    public CouponValidationResponse(boolean valid, String message, String code, Double discountAmount, Double newTotal) {
        this.valid = valid;
        this.message = message;
        this.code = code;
        this.discountAmount = discountAmount;
        this.newTotal = newTotal;
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }

    public Double getNewTotal() { return newTotal; }
    public void setNewTotal(Double newTotal) { this.newTotal = newTotal; }
}
