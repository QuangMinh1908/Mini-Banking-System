package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Số tài khoản
    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;

    // Ngày mở tài khoản
    @Column(name = "date_open", nullable = false, updatable = false)
    private LocalDateTime dateOpen;

    // Số dư
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "fromAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> sentTransactions;

    @OneToMany(mappedBy = "toAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> receivedTransactions;
    
    @Column(name = "account_type")
    private String accountType = "PAYMENT";

    @Column(name = "transaction_limit")
    private String transactionLimit = "50M";

    // tự động gán ngày mở tài khoản khi tạo mới
    @PrePersist
    protected void onCreate() {
        if (this.dateOpen == null) {
            this.dateOpen = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public LocalDateTime getDateOpen() { return dateOpen; }
    public void setDateOpen(LocalDateTime dateOpen) { this.dateOpen = dateOpen; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<Transaction> getSentTransactions() { return sentTransactions; }
    public void setSentTransactions(List<Transaction> sentTransactions) { this.sentTransactions = sentTransactions; }

    public List<Transaction> getReceivedTransactions() { return receivedTransactions; }
    public void setReceivedTransactions(List<Transaction> receivedTransactions) { this.receivedTransactions = receivedTransactions; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getTransactionLimit() { return transactionLimit; }
    public void setTransactionLimit(String transactionLimit) { this.transactionLimit = transactionLimit; }
}