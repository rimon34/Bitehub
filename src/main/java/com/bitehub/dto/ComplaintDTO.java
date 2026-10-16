package com.bitehub.dto;

import com.bitehub.model.Complaint;
import com.bitehub.model.ComplaintStatus;
import java.time.LocalDateTime;

public class ComplaintDTO {
    private Long id;
    private Long orderId;
    private String orderNumber;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String subject;
    private String description;
    private ComplaintStatus status;
    private String adminNotes;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public ComplaintDTO() {}

    public ComplaintDTO(Complaint c) {
        this.id = c.getId();
        if (c.getOrder() != null) {
            this.orderId = c.getOrder().getId();
            this.orderNumber = c.getOrder().getOrderNumber();
        }
        if (c.getCustomer() != null) {
            this.customerId = c.getCustomer().getId();
            this.customerName = c.getCustomer().getName();
            this.customerEmail = c.getCustomer().getEmail();
        }
        this.subject = c.getSubject();
        this.description = c.getDescription();
        this.status = c.getStatus();
        this.adminNotes = c.getAdminNotes();
        this.createdAt = c.getCreatedAt();
        this.resolvedAt = c.getResolvedAt();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { this.status = status; }

    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
