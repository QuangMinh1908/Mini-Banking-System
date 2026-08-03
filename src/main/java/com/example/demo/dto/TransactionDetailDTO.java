package com.example.demo.dto;

import com.example.demo.model.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDetailDTO {
    private String transactionId;
    private TransactionType type;
    private String direction;
    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private String description;
    private String accountNumber;
    private String relatedAccountNumber;

    public TransactionDetailDTO(String transactionId, TransactionType type, String direction, BigDecimal amount, 
                                LocalDateTime transactionDate, String description, String accountNumber, String relatedAccountNumber) {
        this.transactionId = transactionId;
        this.type = type;
        this.direction = direction;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.description = description;
        this.accountNumber = accountNumber;
        this.relatedAccountNumber = relatedAccountNumber;
    }

    // Getters
    public String getTransactionId() { return transactionId; }
    public TransactionType getType() { return type; }
    public String getDirection() { return direction; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public String getDescription() { return description; }
    public String getAccountNumber() { return accountNumber; }
    public String getRelatedAccountNumber() { return relatedAccountNumber; }
}