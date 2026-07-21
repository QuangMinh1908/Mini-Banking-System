package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.model.Account;
import com.example.demo.repository.AccountRepository;
import com.example.demo.service.UserListService;
import com.example.demo.service.AccountListService;
import com.example.demo.config.UserListDTO;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@Controller
public class AdminController {

    private final UserListService userService;
    private final AccountRepository accountRepository;

    public AdminController(UserListService userService, AccountRepository accountRepository) {
        this.userService = userService;
        this.accountRepository = accountRepository;
    }

    // ==========================================
    // 1. QUẢN LÝ KHÁCH HÀNG (USERS)
    // ==========================================
    
    @GetMapping("/admin")
    public String admin(HttpSession session, Model model,
                        @RequestParam(required = false) Long searchId,
                        @RequestParam(required = false) String searchName,
                        @RequestParam(required = false) String searchPhone,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

        Page<UserListDTO> userPage = userService.searchUsers(searchId, searchName, searchPhone, page, size);

        model.addAttribute("userPage", userPage); 
        model.addAttribute("newUser", new User());

        model.addAttribute("searchId", searchId);
        model.addAttribute("searchName", searchName);
        model.addAttribute("searchPhone", searchPhone);
        
        return "admin";
    }

    @PostMapping("/admin/add-user")
    public String addUser(@ModelAttribute("newUser") User newUser, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            newUser.setRole("user");
            userService.createUser(newUser);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm khách hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Không thể thêm khách hàng!");
        }
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/admin");
    }

    @PostMapping("/admin/update-user")
    public String updateUser(@ModelAttribute User updatedUser, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        userService.updateUser(updatedUser);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/admin");
    }

    // ==========================================
    // 2. QUẢN LÝ TÀI KHOẢN (ACCOUNTS)
    // ==========================================
    
    @GetMapping("/admin/account")
    public String adminAccount(HttpSession session, Model model,
                               @RequestParam(required = false) String searchAccNum,
                               @RequestParam(required = false) String searchUsername,
                               @RequestParam(required = false) String searchFullName,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size) {


        Specification<Account> spec = Specification.where(AccountListService.hasUserRole("user"))
                .and(AccountListService.hasAccountNumber(searchAccNum))
                .and(AccountListService.hasFullName(searchFullName));

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Account> accountPage = accountRepository.findAll(spec, pageable);
        
        model.addAttribute("accountPage", accountPage);
        model.addAttribute("searchAccNum", searchAccNum);
        model.addAttribute("searchFullName", searchFullName);
        
        model.addAttribute("username", session.getAttribute("username"));

        return "admin-account";
    }
}