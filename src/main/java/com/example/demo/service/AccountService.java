package com.example.demo.service;

import com.example.demo.model.Account;
import com.example.demo.model.User;
import com.example.demo.model.enums.AccountType;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.AccountUtils;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    private final ApplicationContext applicationContext;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository, ApplicationContext applicationContext) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.applicationContext = applicationContext;
    }

    @Transactional
    public Account createNewAccountForUser(Long userId, String accountType, String transactionLimit, Integer termMonths, java.math.BigDecimal interestRate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng trong hệ thống!"));

        String prefix = "SAVING".equals(accountType) ? "99" : "88";
        Account newAccount = null;
        boolean isSaved = false;

        do {
            String newAccNum = AccountUtils.generateLuhnAccountNumber(prefix);
            try {
                newAccount = applicationContext.getBean(AccountService.class)
                        .saveAccountWithNewTransaction(user, newAccNum, accountType, transactionLimit, termMonths, interestRate);
                isSaved = true; 
            } catch (DataIntegrityViolationException e) {
                System.out.println("Cảnh báo: Trùng số tài khoản " + newAccNum + " (Race Condition). Hệ thống đang tạo lại...");
            }
            } while (!isSaved);
            
            return newAccount;
        }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Account saveAccountWithNewTransaction(User user, String newAccNum, String accountType, String transactionLimit, Integer termMonths, java.math.BigDecimal interestRate) {
        Account account = new Account();
        account.setAccountNumber(newAccNum);
        account.setUser(user);
        account.setBalance(BigDecimal.ZERO);
        account.setAccountType(AccountType.valueOf(accountType.toUpperCase()));
        
        if ("SAVING".equals(accountType)) {
            account.setTransactionLimit(null);
            account.setTermMonths(termMonths);
            account.setInterestRate(interestRate);
        } else {
            account.setTransactionLimit(transactionLimit);
            account.setTermMonths(null);
            account.setInterestRate(null);
        }
        
        // Bắt buộc dùng saveAndFlush để ép Hibernate gửi câu lệnh INSERT xuống DB ngay lập tức.
        // Nhờ vậy, nếu trùng Unique Key, DataIntegrityViolationException sẽ văng ra ngay tại đây.
        return accountRepository.saveAndFlush(account);
    }
}