package com.example.demo.controller;

import com.example.demo.model.Account;
import com.example.demo.repository.AccountRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/transfer")
public class TransferRestController {

    private final AccountRepository accountRepository;

    public TransferRestController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("/lookup-receiver")
    public ResponseEntity<?> lookupReceiver(@RequestParam String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber.trim());
        if (account == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy tài khoản"));
        }
        return ResponseEntity.ok(Map.of("fullName", account.getUser().getFullName()));
    }
}