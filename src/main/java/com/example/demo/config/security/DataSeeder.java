package com.example.demo.config.security;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Lấy toàn bộ user hiện có trong Database
        List<User> users = userRepository.findAll();

        for (User user : users) {
            if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
                
                String rawPassword = user.getPassword();
                String encodedPassword = passwordEncoder.encode(rawPassword);
                
                user.setPassword(encodedPassword);
                userRepository.save(user);
            }
        }
    }
}
