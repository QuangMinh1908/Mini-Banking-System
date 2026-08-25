package com.example.demo.dto;

import com.example.demo.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DashboardTransactionDTO {
    private Long id;
    private String transactionId;
    private TransactionType type;
    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private String direction;
    private String accountNumber;

    public DashboardTransactionDTO(Long id, String transactionId, TransactionType type, BigDecimal amount, LocalDateTime transactionDate, String direction,
                                    String accountNumber) {
        this.id = id;
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.direction = direction;
        this.accountNumber = accountNumber;
    }
    // Getters
    public Long getId() { return id; }
    public String getTransactionId() { return transactionId; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public String getDirection() { return direction; }
    public String getAccountNumber() { return accountNumber; }
}