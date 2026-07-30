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

    public DashboardTransactionDTO(Long id, String transactionId, TransactionType type, BigDecimal amount, LocalDateTime transactionDate) {
        this.id = id;
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.transactionDate = transactionDate;
    }
    // Getters
    public Long getId() { return id; }
    public String getTransactionId() { return transactionId; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
}