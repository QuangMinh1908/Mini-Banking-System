package com.example.demo.config.security;

import java.time.LocalDateTime;

public class UserUpdateRequestDTO {
    private Long id;
    private String username;
    private String status;
    private LocalDateTime requestDate;
    
    // Các trường thay đổi
    private String newFullName;
    private String newPhoneNumber;
    private String newEmail;
    private String newAddress;
    private String newGender;

    public UserUpdateRequestDTO(Long id, String username, String status, LocalDateTime requestDate, 
                                String newFullName, String newPhoneNumber, String newEmail, 
                                String newAddress, String newGender) {
        this.id = id;
        this.username = username;
        this.status = status;
        this.requestDate = requestDate;
        this.newFullName = newFullName;
        this.newPhoneNumber = newPhoneNumber;
        this.newEmail = newEmail;
        this.newAddress = newAddress;
        this.newGender = newGender;
    }

    // --- CÁC HÀM GETTER ---
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getStatus() { return status; }
    public LocalDateTime getRequestDate() { return requestDate; }
    public String getNewFullName() { return newFullName; }
    public String getNewPhoneNumber() { return newPhoneNumber; }
    public String getNewEmail() { return newEmail; }
    public String getNewAddress() { return newAddress; }
    public String getNewGender() { return newGender; }
}