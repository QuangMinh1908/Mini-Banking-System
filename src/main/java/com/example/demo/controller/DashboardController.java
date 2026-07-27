package com.example.demo.controller;

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
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {
                                   
        String role = (String) session.getAttribute("role");
        if (!"user".equals(role)) {
            return "admin".equals(role) ? "redirect:/admin" : "redirect:/login";
        }

        Long currentUserId = (Long) session.getAttribute("userId");
        User currentUser = userRepository.findById(currentUserId).orElseThrow();
        
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("user", currentUser);

        // 1. Dùng Specification để lấy TOÀN BỘ tài khoản của khách hàng
        List<Account> userAccounts = accountRepository.findAll(AccountListService.hasUserId(currentUserId));
        model.addAttribute("accounts", userAccounts);

        // 2. Dùng Specification + Pageable để lấy Lịch sử giao dịch (10 dòng/trang, mới nhất xếp trước)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate"));
        Page<Transaction> txPage = transactionRepository.findAll(TransactionSpecification.isRelatedToUserId(currentUserId), pageable);

        // 3. Đẩy dữ liệu ra view
        model.addAttribute("txPage", txPage);
        model.addAttribute("hasTransactions", txPage.hasContent());

        Transaction latestTx = txPage.hasContent() ? txPage.getContent().get(0) : null;
        model.addAttribute("latestTransaction", latestTx);
        model.addAttribute("newTransactionForm", new Transaction());

        return "dashboard";
    }
}