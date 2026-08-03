package com.example.demo.model;

import com.example.demo.model.enums.AccountType;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "accounts")
public class Account {

    // ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Optimistic Locking của JPA/Hibernate
    @Version
    private Long version;

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

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type")
    private AccountType accountType = AccountType.PAYMENT;

    @Column(name = "transaction_limit")
    private String transactionLimit = "50M";

    @Column(name = "interest_rate", precision = 5, scale = 2)
    private java.math.BigDecimal interestRate;

    @Column(name = "term_months")
    private Integer termMonths;

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

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public LocalDateTime getDateOpen() { return dateOpen; }
    public void setDateOpen(LocalDateTime dateOpen) { this.dateOpen = dateOpen; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }

    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }

    public String getTransactionLimit() { return transactionLimit; }
    public void setTransactionLimit(String transactionLimit) { this.transactionLimit = transactionLimit; }

    public java.math.BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(java.math.BigDecimal interestRate) { this.interestRate = interestRate; }

    public Integer getTermMonths() { return termMonths; }
    public void setTermMonths(Integer termMonths) { this.termMonths = termMonths; }
}