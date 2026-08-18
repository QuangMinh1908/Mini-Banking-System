package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.model.Account;
import com.example.demo.repository.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountService accountService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AccountService accountService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountService = accountService;
    }

    @Transactional
    public Account registerNewUser(User user) {
        // 1. Check trùng Tên đăng nhập
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Tên đăng nhập này đã tồn tại!");
        }

        // 2. Check trùng Số điện thoại (Chặn trước khi gọi Database save)
        if (userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại này đã được sử dụng ở tài khoản khác!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("user"); 

        try {
            User savedUser = userRepository.save(user);

            return accountService.createNewAccountForUser(
                    savedUser.getId(),
                    "PAYMENT",
                    "50M",
                    null,
                    null
            );
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Đã có lỗi dữ liệu trùng lặp xảy ra!");
        }
    }
}