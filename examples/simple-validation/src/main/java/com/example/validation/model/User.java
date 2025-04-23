package com.example.validation.model;

import com.example.datavalidation.ValidatedBy;

/**
 * User model class demonstrating basic validation rules.
 */
@ValidatedBy("user-validation.yml")
public class User {
    private String username;
    private String email;
    private String password;
    private Address address;
    
    // Getters and setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Address getAddress() {
        return address;
    }
    
    public void setAddress(Address address) {
        this.address = address;
    }
} 