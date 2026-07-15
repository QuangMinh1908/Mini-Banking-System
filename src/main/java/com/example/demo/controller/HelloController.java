package com.example.demo.controller;

import com.example.demo.model.BankAccount;
import com.example.demo.repository.BankAccountRepository;
import com.example.demo.service.BankAccountService;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class HelloController {

    private final BankAccountRepository bankAccountRepository;
    private final BankAccountService bankAccountService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public HelloController(BankAccountRepository bankAccountRepository, BankAccountService bankAccountService) {
        this.bankAccountRepository = bankAccountRepository;
        this.bankAccountService = bankAccountService;
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
                                RedirectAttributes redirectAttributes,
                                Model model) {
            Optional<BankAccount> accountOpt = bankAccountRepository.findByAccountName(accountname);

            if (accountOpt.isPresent()) {
                BankAccount account = accountOpt.get();
                boolean isPasswordMatch = false;

                    if (account.getPassword().startsWith("$2a$")) {
                        isPasswordMatch = passwordEncoder.matches(password, account.getPassword());
                    } else {
                        isPasswordMatch = account.getPassword().equals(password);
                    }
                    
                    if (isPasswordMatch) {
                        session.setAttribute("role", account.getRole());
                        session.setAttribute("username", account.getAccountName());
                        session.setAttribute("accountId", account.getId());

                        if ("admin".equals(account.getRole())) {
                            redirectAttributes.addFlashAttribute("successMessage", "✅ Đăng nhập thành công! Chào mừng Quản trị viên quay lại E-Bank.");
                            return "redirect:/admin";
                        } else {
                            redirectAttributes.addFlashAttribute("successMessage", "✅ Đăng nhập thành công! Chào mừng " + account.getUsername() + " quay lại.");
                            return "redirect:/dashboard";
                        }
                    }
                }
            
            model.addAttribute("errorMessage", "❌ Tên tài khoản hoặc mật khẩu không đúng. Vui lòng thử lại.");
            return "login";
        }

    @GetMapping("/admin")
    public String admin(HttpSession session, Model model, @RequestParam(name = "keyword", required = false) String keyword) {
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            return "user".equals(role) ? "redirect:/dashboard" : "redirect:/login";
        }

        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("accounts", bankAccountService.searchCustomers(keyword));
        model.addAttribute("newAccount", new BankAccount());
        model.addAttribute("keyword", keyword);
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

    @PostMapping("/admin/add-customer")
    public String addCustomer(@ModelAttribute("newAccount") BankAccount newAccount, RedirectAttributes redirectAttributes) {
        bankAccountService.createCustomer(newAccount);
        redirectAttributes.addFlashAttribute("successMessage", "✅ Thêm khách hàng mới thành công!");
        return "redirect:/admin";
    }

    @PostMapping("/admin/delete-customer/{id}")
    public String deleteCustomer(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        bankAccountService.deleteCustomer(id);
        redirectAttributes.addFlashAttribute("successMessage", "✅ Đã xóa khách hàng thành công!");
        return "redirect:/admin";
    }

    @PostMapping("/admin/update-customer")
    public String updateCustomer(@ModelAttribute BankAccount editAccount, RedirectAttributes redirectAttributes) {
        bankAccountService.updateCustomer(editAccount);
        redirectAttributes.addFlashAttribute("successMessage", "✅ Cập nhật thông tin khách hàng thành công!");
        return "redirect:/admin";
    }
}