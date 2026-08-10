package com.example.demo.controller;

import com.example.demo.dto.TransactionDetailDTO;
import com.example.demo.model.Account;
import com.example.demo.model.Transaction;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.TransactionRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionRestController {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository; // Thêm AccountRepository

    // Tiêm AccountRepository vào constructor
    public TransactionRestController(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTransactionDetail(@PathVariable Long id, HttpSession session) {
        Long currentUserId = (Long) session.getAttribute("userId");
        
        Transaction tx = transactionRepository.findById(id).orElse(null);
        
        // Bảo mật: Kiểm tra xem giao dịch có tồn tại và có thuộc về user đang đăng nhập không
        if (tx == null || !tx.getAccount().getUser().getId().equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Không có quyền truy cập"));
        }

        // Lấy tên của tài khoản liên quan (nếu có)
        String relatedName = null;
        if (tx.getRelatedAccountNumber() != null) {
            Account relatedAcc = accountRepository.findByAccountNumber(tx.getRelatedAccountNumber());
            if (relatedAcc != null && relatedAcc.getUser() != null) {
                relatedName = relatedAcc.getUser().getFullName();
            }
        }

        // Đưa relatedName vào DTO
        TransactionDetailDTO dto = new TransactionDetailDTO(
                tx.getTransactionId(), tx.getType(), tx.getDirection(), tx.getAmount(),
                tx.getTransactionDate(), tx.getDescription(), 
                tx.getAccount().getAccountNumber(), tx.getRelatedAccountNumber(), relatedName
        );
        return ResponseEntity.ok(dto);
    }
}