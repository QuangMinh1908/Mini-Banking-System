package com.example.demo.controller;

import java.util.Collections;
import com.example.demo.model.BankAccount;
import com.example.demo.model.Transaction;
import com.example.demo.repository.BankAccountRepository;
import com.example.demo.repository.TransactionRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;

    public DashboardController(TransactionRepository transactionRepository, BankAccountRepository bankAccountRepository) {
        this.transactionRepository = transactionRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    @GetMapping
    public String displayDashboard(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!"user".equals(role)) {
            return "admin".equals(role) ? "redirect:/admin" : "redirect:/login";
        }

        // Lấy ID đã định danh từ Session
        Long currentAccountId = (Long) session.getAttribute("accountId");
        
        // Truy vấn chính xác tài khoản đang đăng nhập bằng findById
        BankAccount myAccount = bankAccountRepository.findById(currentAccountId).orElseThrow();
        
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("account", myAccount);

        List<Transaction> txList = transactionRepository.findAll();
        Collections.reverse(txList);
        model.addAttribute("transactionList", txList);
        model.addAttribute("hasTransactions", !txList.isEmpty());

        Transaction latestTx = txList.isEmpty() ? null : txList.get(txList.size() - 1);
        model.addAttribute("latestTransaction", latestTx);
        model.addAttribute("newTransactionForm", new Transaction());

        return "dashboard";
    }

    @PostMapping("/create-transaction")
    @Transactional
    public String handleTransactionSubmit(@ModelAttribute("newTransactionForm") Transaction transaction, 
                                          HttpSession session,
                                          RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute("role");
        if (!"user".equals(role)) return "redirect:/login";

        // Định danh người dùng đang giao dịch
        Long currentAccountId = (Long) session.getAttribute("accountId");
        BankAccount myAccount = bankAccountRepository.findById(currentAccountId).orElseThrow();
        
        BigDecimal amount = transaction.getAmount();
        String selectedType = transaction.getType(); 

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            redirectAttributes.addFlashAttribute("txErrorMessage", "❌ Số tiền giao dịch phải lớn hơn 0 VND!");
            return "redirect:/dashboard";
        }

        if ("Rút tiền".equals(selectedType) || "Chuyển khoản".equals(selectedType)) {
            if (myAccount.getBalance().compareTo(amount) < 0) {
                redirectAttributes.addFlashAttribute("txErrorMessage", "❌ Số dư tài khoản không đủ để thực hiện giao dịch này!");
                return "redirect:/dashboard";
            }
            myAccount.setBalance(myAccount.getBalance().subtract(amount));
            transaction.setType("Rút tiền".equals(selectedType) ? "WITHDRAW" : "TRANSFER");
        } else if ("Nạp tiền".equals(selectedType)) {
            myAccount.setBalance(myAccount.getBalance().add(amount));
            transaction.setType("DEPOSIT");
        }
        
        bankAccountRepository.save(myAccount);

        // Lưu đúng ID của người tạo vào lịch sử giao dịch (ép kiểu về Integer theo Model cũ của bạn)
        transaction.setAccountId(currentAccountId.intValue());
        transaction.setTimestamp(LocalDateTime.now());
        if (transaction.getTransactionId() == null || transaction.getTransactionId().isBlank()) {
            transaction.setTransactionId("TXN-" + System.currentTimeMillis());
        }
        transactionRepository.save(transaction);

        redirectAttributes.addFlashAttribute("txSuccessMessage", "✅ Giao dịch " + selectedType + " số tiền " + String.format("%,.0f", amount) + " VND thành công!");
        return "redirect:/dashboard";
    }
}