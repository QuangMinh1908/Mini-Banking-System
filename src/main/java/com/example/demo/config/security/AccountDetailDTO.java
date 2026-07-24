package com.example.demo.config.security;

import java.time.LocalDateTime;

public class AccountDetailDTO {
    private String accountNumber;
    private LocalDateTime dateOpen;
    private Long ownerId;
    private String ownerName;
    private String ownerPhone;
    private String ownerEmail;
    private String accountType;
    private String transactionLimit;

public AccountDetailDTO(String accountNumber, LocalDateTime dateOpen, Long ownerId, 
                               String ownerName, String ownerPhone, String ownerEmail,
                               String accountType, String transactionLimit) {
        this.accountNumber = accountNumber;
        this.dateOpen = dateOpen;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
        this.ownerEmail = ownerEmail;
        this.accountType = accountType;
        this.transactionLimit = transactionLimit;
    }

    public String getAccountNumber() { return accountNumber; }
    public LocalDateTime getDateOpen() { return dateOpen; }
    public Long getOwnerId() { return ownerId; }
    public String getOwnerName() { return ownerName; }
    public String getOwnerPhone() { return ownerPhone; }
    public String getOwnerEmail() { return ownerEmail; }
    public String getAccountType() { return accountType; }
    public String getTransactionLimit() { return transactionLimit; }
}