package com.bitehub.dto;

import jakarta.validation.constraints.NotBlank;

public class RefundRequest {
    @NotBlank(message = "Refund reason is required")
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
