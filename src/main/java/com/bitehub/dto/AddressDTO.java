package com.bitehub.dto;

import com.bitehub.model.Address;

public class AddressDTO {
    private Long id;
    private String label;
    private String streetAddress;
    private String city;
    private String postalCode;
    private String contactPhone;
    private boolean isDefault;

    public AddressDTO() {}

    public AddressDTO(Address a) {
        this.id = a.getId();
        this.label = a.getLabel();
        this.streetAddress = a.getStreetAddress();
        this.city = a.getCity();
        this.postalCode = a.getPostalCode();
        this.contactPhone = a.getContactPhone();
        this.isDefault = a.isDefault();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getStreetAddress() { return streetAddress; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
}
