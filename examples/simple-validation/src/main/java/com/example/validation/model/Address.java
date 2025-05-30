package com.example.validation.model;

import com.example.datavalidation.ValidatedBy;

/**
 * Address model class for user addresses.
 */
@ValidatedBy("address-validation.yml")
public class Address {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    
    // Getters and setters
    public String getStreet() {
        return street;
    }
    
    public void setStreet(String street) {
        this.street = street;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getZipCode() {
        return zipCode;
    }
    
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
} 