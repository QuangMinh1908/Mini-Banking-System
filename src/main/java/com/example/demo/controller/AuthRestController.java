package com.example.demo.controller;

import com.example.demo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final UserRepository userRepository;

    public AuthRestController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/check-step1")
    public ResponseEntity<Map<String, String>> checkStep1(
            @RequestParam String username,
            @RequestParam String phoneNumber) {

        Map<String, String> response = new HashMap<>();

        if (userRepository.findByUsername(username).isPresent()) {
            response.put("error", "Tên đăng nhập này đã tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }

        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            response.put("error", "Số điện thoại này đã được sử dụng!");
            return ResponseEntity.badRequest().body(response);
        }

        response.put("status", "ok");
        return ResponseEntity.ok(response);
    }
}