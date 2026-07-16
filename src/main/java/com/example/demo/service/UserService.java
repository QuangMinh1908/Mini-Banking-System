package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> searchCustomers(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return userRepository.searchCustomers(keyword.trim());
        }
        return userRepository.findByRoleNot("admin");
    }

    public void createCustomer(User user) {
        user.setRole("user"); 
        userRepository.save(user);
    }

    public void updateCustomer(User updatedUser) {
        User existingUser = userRepository.findById(updatedUser.getId()).orElse(null);
        if (existingUser != null) {
            existingUser.setFullName(updatedUser.getFullName());
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
            userRepository.save(existingUser);
        }
    }

    public void deleteCustomer(Long id) {
        userRepository.deleteById(id);
    }
}