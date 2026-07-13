package com.example.demo.model;
import java.math.BigDecimal;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "account_id")
    private Integer accountId; // Cột mới thêm để liên kết với bảng accounts

    @Column(name = "transaction_type")
    private String type;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "transaction_date") // Cập nhật lại tên cột cho khớp SQL
    private LocalDateTime timestamp;

    public Transaction() {}

    // Getters và Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public Integer getAccountId() { return accountId; }
    public void setAccountId(Integer accountId) { this.accountId = accountId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}