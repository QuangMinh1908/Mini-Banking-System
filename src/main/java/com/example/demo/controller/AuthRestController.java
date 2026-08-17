package com.example.demo.controller;

import com.example.demo.dto.CurrentUserDTO;
import com.example.demo.dto.RegisterRequestDTO;
import com.example.demo.dto.ResponseDTO;
import com.example.demo.model.Account;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final UserRepository userRepository;
    private final UserService userService;

    public AuthRestController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/check-step1")
    public ResponseEntity<Map<String, String>> checkStep1(
            @RequestParam String username,
            @RequestParam String phoneNumber) {

        Map<String, String> response = new HashMap<>();

        boolean isUsernameExist = userRepository.findByUsername(username).isPresent();
        boolean isPhoneExist = userRepository.existsByPhoneNumber(phoneNumber);

        if (isUsernameExist || isPhoneExist) {
            response.put("error", "Tên đăng nhập hoặc số điện thoại đã tồn tại trên hệ thống!");
            return ResponseEntity.badRequest().body(response);
        }

        response.put("status", "ok");
        return ResponseEntity.ok(response);
    }


    @GetMapping("/me")
    public ResponseEntity<CurrentUserDTO> me(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        CurrentUserDTO dto = new CurrentUserDTO(
                user.getId(), user.getUsername(), user.getFullName(), user.getPhoneNumber(),
                user.getEmail(), user.getGender(), user.getAddress(), user.getCreatedAt(), user.getRole());
        return ResponseEntity.ok(dto);
    }


    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<String>> register(@Valid @RequestBody RegisterRequestDTO form,
                                                          HttpServletRequest request) throws Exception {
        String rawPassword = form.getPassword();

        User newUser = new User();
        newUser.setUsername(form.getUsername());
        newUser.setPassword(rawPassword);
        newUser.setFullName(form.getFullName());
        newUser.setPhoneNumber(form.getPhoneNumber());
        newUser.setEmail(form.getEmail());
        newUser.setAddress(form.getAddress());
        newUser.setGender(form.getGender());

        Account newAccount = userService.registerNewUser(newUser);

        request.login(newUser.getUsername(), rawPassword);
        request.getSession().setAttribute("username", newUser.getUsername());
        request.getSession().setAttribute("role", "user");
        request.getSession().setAttribute("userId", newUser.getId());

        return ResponseEntity.ok(ResponseDTO.success("Đăng ký thành công", newAccount.getAccountNumber()));
    }
}