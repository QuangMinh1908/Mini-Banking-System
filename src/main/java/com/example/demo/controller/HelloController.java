package com.example.demo.controller;

import com.example.demo.model.BankAccount;
import com.example.demo.repository.BankAccountRepository;
import java.util.List;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class HelloController {

    private final BankAccountRepository bankAccountRepository;

    public HelloController(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if ("user".equals(role)) {return "redirect:/dashboard";}
        if ("admin".equals(role)) {return "redirect:/admin";}
        return "login";
    }

    @PostMapping("/login")
        public String handleLogin(@RequestParam("accountname") String accountname,
                                @RequestParam("password") String password,
                                HttpSession session,
                                RedirectAttributes redirectAttributes, // Dùng cái này để hiện thông báo 1 lần
                                Model model) {
            Optional<BankAccount> accountOpt = bankAccountRepository.findByAccountName(accountname);

            if (accountOpt.isPresent() && accountOpt.get().getPassword().equals(password)) {
                    BankAccount account = accountOpt.get();

                    session.setAttribute("role", account.getRole());
                    session.setAttribute("username", account.getUsername());
                    session.setAttribute("accountId", account.getId());

                    if ("admin".equals(account.getRole())) {
                        redirectAttributes.addFlashAttribute("successMessage", "✅ Đăng nhập thành công! Chào mừng Quản trị viên quay lại E-Bank.");
                        return "redirect:/admin";
                    } else {
                        redirectAttributes.addFlashAttribute("successMessage", "✅ Đăng nhập thành công! Chào mừng " + account.getUsername() + " quay lại E-Bank.");
                        return "redirect:/dashboard";
                    }
                }

                model.addAttribute("errorMessage", "Tài khoản hoặc mật khẩu không chính xác.");
                return "login";        
        }

    // Hàm Admin đã được cập nhật logic lấy dữ liệu từ DB
    @GetMapping("/admin")
    public String admin(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            return "user".equals(role) ? "redirect:/dashboard" : "redirect:/login";
        }

        model.addAttribute("username", session.getAttribute("username"));

        model.addAttribute("accounts", bankAccountRepository.findByRole("user"));
        return "admin";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }

    @GetMapping("/success")
    public String success() {
        return "success";
    }
}