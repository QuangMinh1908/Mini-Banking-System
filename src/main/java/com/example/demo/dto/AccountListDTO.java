package com.example.demo.dto;

import java.time.LocalDateTime;

public class AccountListDTO {
    private String accountNumber;
    private LocalDateTime dateOpen;
    private Owner user;

    public AccountListDTO(String accountNumber, LocalDateTime dateOpen, String ownerFullName) {
        this.accountNumber = accountNumber;
        this.dateOpen = dateOpen;
        this.user = new Owner(ownerFullName);
    }

    public String getAccountNumber() { return accountNumber; }
    public LocalDateTime getDateOpen() { return dateOpen; }
    public Owner getUser() { return user; }

    public static class Owner {
        private String fullName;

        public Owner(String fullName) {
            this.fullName = fullName;
        }

        public String getFullName() { return fullName; }
    }
}
