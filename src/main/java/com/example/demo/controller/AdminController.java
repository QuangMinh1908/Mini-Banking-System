package com.example.demo.controller;

import com.example.demo.model.Account;
import com.example.demo.repository.AccountRepository;
import com.example.demo.service.UserListService;
import com.example.demo.service.AccountListService;
import com.example.demo.dto.UserListDTO;
import com.example.demo.dto.UserUpdateFormDTO;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
import org.springframework.validation.BindingResult;


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
        model.addAttribute("searchId", searchId);
        model.addAttribute("searchName", searchName);
        model.addAttribute("searchPhone", searchPhone);
        
        return "admin";
    }

    @PostMapping("/admin/update-user")
    public String updateUser(@Valid @ModelAttribute UserUpdateFormDTO updatedUser, 
                            BindingResult bindingResult,
                            @RequestParam(required = false) String detail, 
                            HttpServletRequest request, 
                            RedirectAttributes redirectAttributes) {
    
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu nhập không hợp lệ, vui lòng kiểm tra lại!");
            return "redirect:" + resolveSafeRedirect(request.getHeader("Referer"));
        }
    
        userService.createUpdateRequest(updatedUser, detail);

        redirectAttributes.addFlashAttribute("successMessage", "Yêu cầu thay đổi thông tin đã được gửi lên hệ thống và đang chờ xét duyệt!");

        return "redirect:" + resolveSafeRedirect(request.getHeader("Referer"));
    }

    private static final java.util.Set<String> ALLOWED_REDIRECT_PATHS = java.util.Set.of("/admin", "/admin/account");
    /**
     * Chỉ tin tưởng phần PATH của Referer (bỏ qua scheme/host hoàn toàn), và chỉ chấp nhận
     * nếu path nằm trong allowlist bên trên. Nhờ vậy kết quả trả về luôn là một đường dẫn
     * nội bộ bắt đầu bằng "/", không thể bị lợi dụng để redirect ra domain khác (Open Redirect).*/
    private String resolveSafeRedirect(String referer) {
        if (referer != null) {
            try {
                java.net.URI uri = java.net.URI.create(referer);
                String path = uri.getPath();
                if (path != null && ALLOWED_REDIRECT_PATHS.contains(path)) {
                    String query = uri.getQuery();
                    return path + (query != null ? "?" + query : "");
                }
            } catch (IllegalArgumentException ignored) {}
        }
        return "/admin";
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
                .and(AccountListService.hasFullName(searchFullName))
                .and(AccountListService.hasUsername(searchUsername));

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Account> accountPage = accountRepository.findAll(spec, pageable);

        model.addAttribute("accountPage", accountPage);
        model.addAttribute("searchAccNum", searchAccNum);
        model.addAttribute("searchFullName", searchFullName);
        model.addAttribute("searchUsername", searchUsername);

        return "admin-account";
    }
}