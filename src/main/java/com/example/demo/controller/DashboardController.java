package com.example.demo.controller;

import com.example.demo.dto.DashboardAccountDTO;
import com.example.demo.dto.DashboardTransactionDTO;
import com.example.demo.model.Account;
import com.example.demo.model.Transaction;
import com.example.demo.model.User;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AccountListService;
import com.example.demo.service.TransactionSpecification;

import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public DashboardController(TransactionRepository transactionRepository, UserRepository userRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @GetMapping
    public String displayDashboard(HttpSession session, Model model,
                                   @RequestParam(defaultValue = "10") int size) {
        
        Long currentUserId = (Long) session.getAttribute("userId");
        User currentUser = userRepository.findById(currentUserId).orElseThrow();
        
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("user", currentUser); 
        
        // 1. MAP ACCOUNT SANG DTO
        List<Account> userAccounts = accountRepository.findAll(AccountListService.hasUserId(currentUserId));
        List<DashboardAccountDTO> accountDTOs = userAccounts.stream()
            .map(acc -> new DashboardAccountDTO(
                    acc.getAccountNumber(), 
                    acc.getAccountType(), 
                    acc.getTransactionLimit(), 
                    acc.getBalance(), 
                    acc.getDateOpen(),
                    acc.getInterestRate(),
                    acc.getTermMonths()))
            .toList();
        model.addAttribute("accounts", accountDTOs);

        // 2. MAP TRANSACTION SANG DTO
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "transactionDate", "id"));
        Page<Transaction> txPage = transactionRepository.findAll(
                TransactionSpecification.isRelatedToUserIdWithCursor(currentUserId, null, null), pageable);
                
        List<DashboardTransactionDTO> transactionDTOs = txPage.getContent().stream()
            .map(tx -> new DashboardTransactionDTO(
                    tx.getId(), 
                    tx.getTransactionId(), 
                    tx.getType(), 
                    tx.getAmount(), 
                    tx.getTransactionDate(),
                    tx.getDirection(),
                    tx.getAccount().getAccountNumber()))
            .toList();
            
        model.addAttribute("transactions", transactionDTOs);
        model.addAttribute("hasTransactions", !transactionDTOs.isEmpty());
        
        DashboardTransactionDTO latestTx = transactionDTOs.isEmpty() ? null : transactionDTOs.get(0);
        model.addAttribute("latestTransaction", latestTx);
        
        model.addAttribute("newTransactionForm", new Transaction());
        return "dashboard";
    }

    @GetMapping("/transactions/more")
    public String loadMoreTransactions(HttpSession session, Model model,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastDate,
                                       @RequestParam Long lastId,
                                       @RequestParam(defaultValue = "10") int size) {
                                       
        Long currentUserId = (Long) session.getAttribute("userId");
        
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "transactionDate", "id"));
        Page<Transaction> txPage = transactionRepository.findAll(
                TransactionSpecification.isRelatedToUserIdWithCursor(currentUserId, lastDate, lastId), pageable);

        // MAP TRANSACTION SANG DTO KHI LOAD MORE
        List<DashboardTransactionDTO> transactionDTOs = txPage.getContent().stream()
            .map(tx -> new DashboardTransactionDTO(
                    tx.getId(), 
                    tx.getTransactionId(), 
                    tx.getType(), 
                    tx.getAmount(), 
                    tx.getTransactionDate(),
                    tx.getDirection(),
                    tx.getAccount().getAccountNumber()))
            .toList();

        model.addAttribute("transactions", transactionDTOs);
        return "dashboard :: txItems"; 
    }

    @GetMapping("/history")
    public String displayTransactionHistoryPage(HttpSession session, Model model, 
                                                @RequestParam(defaultValue = "20") int size) {
        Long currentUserId = (Long) session.getAttribute("userId");
        User currentUser = userRepository.findById(currentUserId).orElseThrow();
        
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("user", currentUser); 
        
        // Lấy danh sách giao dịch
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "transactionDate", "id"));
        Page<Transaction> txPage = transactionRepository.findAll(
                TransactionSpecification.isRelatedToUserIdWithCursor(currentUserId, null, null), pageable);
                
        List<DashboardTransactionDTO> transactionDTOs = txPage.getContent().stream()
            .map(tx -> new DashboardTransactionDTO(
                    tx.getId(), tx.getTransactionId(), tx.getType(), tx.getAmount(), 
                    tx.getTransactionDate(), tx.getDirection(), tx.getAccount().getAccountNumber()))
            .toList();
            
        model.addAttribute("transactions", transactionDTOs);
        model.addAttribute("hasTransactions", !transactionDTOs.isEmpty());
        
        return "dashboard-history";
    }
}