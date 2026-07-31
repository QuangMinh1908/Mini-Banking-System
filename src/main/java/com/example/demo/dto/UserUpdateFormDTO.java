package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;

public class UserUpdateFormDTO {

    @NotNull(message = "Thiếu ID khách hàng")
    private Long id;

    private String fullName;
    private String phoneNumber;
    private String email;
    private String address;
    private String gender;

    public UserUpdateFormDTO() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
}