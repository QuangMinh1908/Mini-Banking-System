package com.example.demo.dto;

import com.example.demo.model.enums.AccountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DashboardAccountDTO {
    private String accountNumber;
    private AccountType accountType; 
    private String transactionLimit;
    private BigDecimal balance;
    private LocalDateTime dateOpen;
    private BigDecimal interestRate;
    private Integer termMonths;

    public DashboardAccountDTO(String accountNumber, AccountType accountType, String transactionLimit, 
                               BigDecimal balance, LocalDateTime dateOpen, BigDecimal interestRate, Integer termMonths) {
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.transactionLimit = transactionLimit;
        this.balance = balance;
        this.dateOpen = dateOpen;
        this.interestRate = interestRate;
        this.termMonths = termMonths;
    }

    // Getter
    public String getAccountNumber() { return accountNumber; }
    public AccountType getAccountType() { return accountType; }
    public String getTransactionLimit() { return transactionLimit; }
    public BigDecimal getBalance() { return balance; }
    public LocalDateTime getDateOpen() { return dateOpen; }
    public BigDecimal getInterestRate() { return interestRate; }
    public Integer getTermMonths() { return termMonths; }
}