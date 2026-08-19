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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng!"));

        String prefix = "SAVING".equals(accountType) ? "99" : "88";
        String newAccNum;

        // Sinh số ngẫu nhiên và check trùng trước khi lưu
        do {
            newAccNum = AccountUtils.generateLuhnAccountNumber(prefix);
        } while (accountRepository.existsByAccountNumber(newAccNum));

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
        
        return accountRepository.save(account);
    }
}