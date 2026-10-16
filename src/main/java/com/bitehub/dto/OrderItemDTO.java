package com.bitehub.dto;

import com.bitehub.model.OrderItem;

public class OrderItemDTO {
    private Long id;
    private Long menuItemId;
    private String itemName;
    private Double itemPrice;
    private Integer quantity;
    private Double subtotal;

    public OrderItemDTO() {}

    public OrderItemDTO(OrderItem item) {
        this.id = item.getId();
        if (item.getMenuItem() != null) {
            this.menuItemId = item.getMenuItem().getId();
        }
        this.itemName = item.getItemName();
        this.itemPrice = item.getItemPrice();
        this.quantity = item.getQuantity();
        this.subtotal = item.getSubtotal();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMenuItemId() { return menuItemId; }
    public void setMenuItemId(Long menuItemId) { this.menuItemId = menuItemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public Double getItemPrice() { return itemPrice; }
    public void setItemPrice(Double itemPrice) { this.itemPrice = itemPrice; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
}
