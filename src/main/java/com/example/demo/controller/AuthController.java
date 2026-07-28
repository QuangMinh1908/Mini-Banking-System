package com.example.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

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

    @GetMapping("/success")
    public String success() {
        return "success";
    }
}