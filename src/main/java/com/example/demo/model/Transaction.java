package com.example.demo.model;

import com.example.demo.model.enums.TransactionType;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "transactions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"transaction_id", "direction"})
})
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id")
    private String transactionId;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "related_account_number")
    private String relatedAccountNumber;

    @Column(name = "direction")
    private String direction;

    private BigDecimal amount;
    private String description;
    private LocalDateTime transactionDate;

    // --- Getters and Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getRelatedAccountNumber() {
        return relatedAccountNumber;
    }

    public void setRelatedAccountNumber(String relatedAccountNumber) {
        this.relatedAccountNumber = relatedAccountNumber;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
}