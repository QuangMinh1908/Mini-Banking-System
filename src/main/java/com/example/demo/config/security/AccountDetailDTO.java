package com.example.demo.config.security;

import java.time.LocalDateTime;

public class AccountDetailDTO {
    private String accountNumber;
    private LocalDateTime dateOpen;

    public AccountDetailDTO(String accountNumber, LocalDateTime dateOpen) {
        this.accountNumber = accountNumber;
        this.dateOpen = dateOpen;
    }

    // Getters and Setters
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public LocalDateTime getDateOpen() { return dateOpen; }
    public void setDateOpen(LocalDateTime dateOpen) { this.dateOpen = dateOpen; }
}