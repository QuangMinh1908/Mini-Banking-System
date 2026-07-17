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

public List<User> searchUsers(Long id, String name, String phone) {
    boolean isIdEmpty = (id == null);
    boolean isNameEmpty = (name == null || name.trim().isEmpty());
    boolean isPhoneEmpty = (phone == null || phone.trim().isEmpty());

    if (isIdEmpty && isNameEmpty && isPhoneEmpty) {
        return userRepository.findByRoleNot("admin");
    }
    
    return userRepository.searchUsers(id, name, phone);    
}

    public void createUser(User user) {
        user.setRole("user"); 
        userRepository.save(user);
    }

    public void updateUser(User updatedUser) {
        User existingUser = userRepository.findById(updatedUser.getId()).orElse(null);
        if (existingUser != null) {
            existingUser.setFullName(updatedUser.getFullName());
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
            userRepository.save(existingUser);
        }
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}