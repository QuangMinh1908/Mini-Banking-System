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
import org.springframework.dao.DataIntegrityViolationException;

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
    public String admin(HttpSession session, Model model,
                        @RequestParam(required = false) Long searchId,
                        @RequestParam(required = false) String searchName,
                        @RequestParam(required = false) String searchPhone) {

        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            return "user".equals(role) ? "redirect:/dashboard" : "redirect:/login";
        }

        List<User> users = userService.searchUsers(searchId, searchName, searchPhone);        

        model.addAttribute("users", users);
        model.addAttribute("newUser", new User());
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

    @PostMapping("/admin/add-user")
    public String addUser(@ModelAttribute("newUser") User newUser, RedirectAttributes redirectAttributes) {
        try {
            newUser.setRole("user");
            userService.createUser(newUser);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm khách hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Không thể thêm khách hàng!");
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/update-user")
    public String updateUser(@ModelAttribute User updatedUser, RedirectAttributes redirectAttributes) {
        userService.updateUser(updatedUser);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        return "redirect:/admin";
    }

@PostMapping("/admin/delete-user/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa khách hàng thành công!");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa! Khách hàng này đã phát sinh giao dịch trên hệ thống.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi không xác định khi xóa khách hàng.");
        }
        return "redirect:/admin";
    }
}