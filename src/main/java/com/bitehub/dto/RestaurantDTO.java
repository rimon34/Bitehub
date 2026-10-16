package com.bitehub.dto;

import com.bitehub.model.Restaurant;

public class RestaurantDTO {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private String cuisineType;
    private String address;
    private String phone;
    private String imageUrl;
    private String bannerUrl;
    private boolean isOpen;
    private boolean isVerified;
    private Double rating;
    private Integer totalReviews;

    public RestaurantDTO() {}

    public RestaurantDTO(Restaurant r) {
        this.id = r.getId();
        if (r.getUser() != null) {
            this.userId = r.getUser().getId();
        }
        this.name = r.getName();
        this.description = r.getDescription();
        this.cuisineType = r.getCuisineType();
        this.address = r.getAddress();
        this.phone = r.getPhone();
        this.imageUrl = r.getImageUrl();
        this.bannerUrl = r.getBannerUrl();
        this.isOpen = r.isOpen();
        this.isVerified = r.isVerified();
        this.rating = r.getRating();
        this.totalReviews = r.getTotalReviews();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCuisineType() { return cuisineType; }
    public void setCuisineType(String cuisineType) { this.cuisineType = cuisineType; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }

    public boolean isOpen() { return isOpen; }
    public void setOpen(boolean open) { isOpen = open; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }
}
