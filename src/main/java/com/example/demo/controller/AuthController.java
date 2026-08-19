package com.example.demo.controller;

import com.example.demo.dto.RegisterRequestDTO;
import com.example.demo.model.Account;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String role = authentication.getAuthorities().iterator().next().getAuthority();
            if ("admin".equals(role)) return "redirect:/admin";
            if ("user".equals(role)) return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registerForm", new RegisterRequestDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("registerForm") RegisterRequestDTO form,
                                BindingResult bindingResult,
                                HttpServletRequest request, RedirectAttributes redirectAttributes, Model model) {

        if (bindingResult.hasErrors()) {
            FieldError firstError = bindingResult.getFieldErrors().get(0);
            model.addAttribute("errorMessage", firstError.getDefaultMessage());
            return "register";
        }

        String rawPassword = form.getPassword();
        try {
            User newUser = new User();
            newUser.setUsername(form.getUsername());
            newUser.setPassword(rawPassword);
            newUser.setFullName(form.getFullName());
            newUser.setPhoneNumber(form.getPhoneNumber());
            newUser.setEmail(form.getEmail());
            newUser.setAddress(form.getAddress());
            newUser.setGender(form.getGender());

            // Tạo User và nhận về Account
            Account newAccount = userService.registerNewUser(newUser);

            // 1. Tự động đăng nhập người dùng vào Spring Security
            request.login(newUser.getUsername(), rawPassword);

            // 2. Gắn thông tin vào Session (để dùng trong Dashboard)
            request.getSession().setAttribute("username", newUser.getUsername());
            request.getSession().setAttribute("role", "user");
            request.getSession().setAttribute("userId", newUser.getId());

            // 3. Truyền số tài khoản qua trang Success
            redirectAttributes.addFlashAttribute("accountNumber", newAccount.getAccountNumber());

            return "redirect:/register/success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/register/success")
    public String registerSuccess(Model model) {
        if (!model.containsAttribute("accountNumber")) {
            return "redirect:/dashboard";
        }
        return "register-success";
    }

    @GetMapping("/success")
    public String success() {
        return "success";
    }
}