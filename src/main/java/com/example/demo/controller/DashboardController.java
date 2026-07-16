package com.example.demo.controller;

import com.example.demo.model.Transaction;
import com.example.demo.model.User;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Collections;


@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public DashboardController(TransactionRepository transactionRepository, UserRepository userRepository  ) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String displayDashboard(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!"user".equals(role)) {
            return "admin".equals(role) ? "redirect:/admin" : "redirect:/login";
        }

        Long currentUserId = (Long) session.getAttribute("userId");
        
        User currentUser = userRepository.findById(currentUserId).orElseThrow();
        
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("user", currentUser);

        // Chỉ lấy Account thật từ Database, không tạo ảo nữa
        if (currentUser.getAccounts() != null && !currentUser.getAccounts().isEmpty()) {
            model.addAttribute("account", currentUser.getAccounts().get(0));
        }

        // --- Phần Transaction ---
        List<Transaction> txList = transactionRepository.findAll();
        Collections.reverse(txList);
        model.addAttribute("transactionList", txList);
        model.addAttribute("hasTransactions", !txList.isEmpty());

        Transaction latestTx = txList.isEmpty() ? null : txList.get(txList.size() - 1);
        model.addAttribute("latestTransaction", latestTx);
        model.addAttribute("newTransactionForm", new Transaction());

        return "dashboard";
    }
}