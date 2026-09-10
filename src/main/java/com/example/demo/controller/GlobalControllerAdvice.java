package com.example.demo.controller;

import com.example.demo.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final TransactionRepository transactionRepository;

    public GlobalControllerAdvice(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @ModelAttribute
    public void addGlobalAttributes(HttpSession session, HttpServletRequest request, Model model) {
        model.addAttribute("currentUri", request.getRequestURI());

        Long userId = (Long) session.getAttribute("userId");
        
        if (userId != null) {
            long unreadCount = transactionRepository.countUnreadByUserId(userId);
            model.addAttribute("unreadCount", unreadCount);
        }
    }
}