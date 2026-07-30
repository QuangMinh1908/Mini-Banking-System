package com.example.demo.service;

import com.example.demo.model.Account;
import com.example.demo.model.User;
import com.example.demo.model.enums.AccountType;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.AccountUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Account createNewAccountForUser(Long userId, String accountType, String transactionLimit, Integer termMonths, java.math.BigDecimal interestRate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng trong hệ thống!"));

        Account newAccount = new Account();
        String prefix = "SAVING".equals(accountType) ? "99" : "88";
        boolean isSaved = false;

        do {
            String newAccNum = AccountUtils.generateLuhnAccountNumber(prefix);
            
            if (!accountRepository.existsByAccountNumber(newAccNum)) {
                newAccount.setAccountNumber(newAccNum);
                newAccount.setUser(user);
                newAccount.setBalance(BigDecimal.ZERO);
                newAccount.setAccountType(AccountType.valueOf(accountType.toUpperCase()));
                
                if ("SAVING".equals(accountType)) {
                    newAccount.setTransactionLimit(null);
                    newAccount.setTermMonths(termMonths);
                    newAccount.setInterestRate(interestRate);
                } else {
                    newAccount.setTransactionLimit(transactionLimit);
                    newAccount.setTermMonths(null);
                    newAccount.setInterestRate(null);
                }
                
                newAccount = accountRepository.save(newAccount);
                isSaved = true;
            } else {
                System.out.println("Cảnh báo: Trùng số tài khoản " + newAccNum + ", hệ thống đang tạo lại...");
            }
        } while (!isSaved);

        return newAccount;
    }
}