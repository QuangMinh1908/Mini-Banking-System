package com.example.demo.dto;

import java.time.LocalDateTime;

public class CurrentUserDTO {
    private Long id;
    private String username;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String gender;
    private String address;
    private LocalDateTime createdAt;
    private String role;

    public CurrentUserDTO(Long id, String username, String fullName, String phoneNumber, String email,
                           String gender, String address, LocalDateTime createdAt, String role) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.gender = gender;
        this.address = address;
        this.createdAt = createdAt;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getGender() { return gender; }
    public String getAddress() { return address; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getRole() { return role; }
}
