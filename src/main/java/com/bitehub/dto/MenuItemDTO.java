package com.bitehub.dto;

import com.bitehub.model.MenuItem;

public class MenuItemDTO {
    private Long id;
    private Long restaurantId;
    private String restaurantName;
    private String name;
    private String description;
    private String category;
    private Double price;
    private String imageUrl;
    private boolean isAvailable;
    private boolean isVegetarian;
    private Integer preparationTimeMinutes;

    public MenuItemDTO() {}

    public MenuItemDTO(MenuItem m) {
        this.id = m.getId();
        if (m.getRestaurant() != null) {
            this.restaurantId = m.getRestaurant().getId();
            this.restaurantName = m.getRestaurant().getName();
        }
        this.name = m.getName();
        this.description = m.getDescription();
        this.category = m.getCategory();
        this.price = m.getPrice();
        this.imageUrl = m.getImageUrl();
        this.isAvailable = m.isAvailable();
        this.isVegetarian = m.isVegetarian();
        this.preparationTimeMinutes = m.getPreparationTimeMinutes();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public boolean isVegetarian() { return isVegetarian; }
    public void setVegetarian(boolean vegetarian) { isVegetarian = vegetarian; }

    public Integer getPreparationTimeMinutes() { return preparationTimeMinutes; }
    public void setPreparationTimeMinutes(Integer preparationTimeMinutes) { this.preparationTimeMinutes = preparationTimeMinutes; }
}
