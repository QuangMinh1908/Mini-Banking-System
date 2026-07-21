package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
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
    public String handleLogin(@RequestParam("username") String username,
                              @RequestParam("password") String password,
                              HttpSession session,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean isPasswordMatch = false;

            if (user.getPassword().startsWith("$2a$")) {
                isPasswordMatch = passwordEncoder.matches(password, user.getPassword());
            } else {
                isPasswordMatch = user.getPassword().equals(password);
            }
            
            if (isPasswordMatch) {
                session.setAttribute("role", user.getRole());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("userId", user.getId());

                if ("admin".equals(user.getRole())) {
                    redirectAttributes.addFlashAttribute("successMessage", "✅ Đăng nhập thành công! Chào mừng Quản trị viên quay lại E-Bank.");
                    return "redirect:/admin";
                } else {
                    redirectAttributes.addFlashAttribute("successMessage", "✅ Đăng nhập thành công! Chào mừng " + user.getUsername() + " quay lại.");
                    return "redirect:/dashboard";
                }
            }
        }
        
        model.addAttribute("errorMessage", "❌ Tên tài khoản hoặc mật khẩu không đúng. Vui lòng thử lại.");
        return "login";
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