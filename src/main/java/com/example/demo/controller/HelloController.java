package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;

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

import java.util.List;
import java.util.Optional;

@Controller
public class HelloController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public HelloController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
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

    @GetMapping("/admin")
    public String admin(HttpSession session, Model model, @RequestParam(name = "keyword", required = false) String keyword) {
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            return "user".equals(role) ? "redirect:/dashboard" : "redirect:/login";
        }

        List<User> users = userService.searchCustomers(keyword);
        model.addAttribute("users", users);
        model.addAttribute("newUser", new User());
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
    public String addCustomer(@ModelAttribute("newUser") User newUser) {
        userService.createCustomer(newUser);
        return "redirect:/admin";
    }

    @PostMapping("/admin/update-customer")
    public String updateCustomer(@ModelAttribute User updatedUser) {
        userService.updateCustomer(updatedUser);
        return "redirect:/admin";
    }

    @PostMapping("/admin/delete-customer/{id}")
    public String deleteCustomer(@PathVariable Long id) {
        userService.deleteCustomer(id);
        return "redirect:/admin";
    }
}