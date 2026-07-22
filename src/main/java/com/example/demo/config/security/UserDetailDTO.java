package com.example.demo.config.security;

import java.time.LocalDateTime;
import java.util.List;

public class UserDetailDTO {
    private Long id;
    private String username;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String address;
    private String gender;
    private LocalDateTime createdAt;
    private List<AccountDetailDTO> accounts;

    public UserDetailDTO(Long id, String username, String fullName, String phoneNumber, 
                         String email, String address, 
                         String gender, LocalDateTime createdAt,
                         List<AccountDetailDTO> accounts) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.gender = gender;
        this.createdAt = createdAt;
        this.accounts = accounts;
    }

    // --- Getters và Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<AccountDetailDTO> getAccounts() { return accounts; }
    public void setAccounts(List<AccountDetailDTO> accounts) { this.accounts = accounts; }
}