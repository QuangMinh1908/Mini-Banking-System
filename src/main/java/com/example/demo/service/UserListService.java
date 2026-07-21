package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.config.UserListDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserListService {

    private final UserRepository userRepository;

    UserListService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<UserListDTO> searchUsers(Long id, String name, String phone, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            predicates.add(cb.notEqual(root.get("role"), "admin"));
            
            if (id != null) {
                predicates.add(cb.equal(root.get("id"), id));
            }
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("fullName")), "%" + name.toLowerCase() + "%"));
            }
            if (phone != null && !phone.trim().isEmpty()) {
                predicates.add(cb.like(root.get("phoneNumber"), "%" + phone + "%"));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> userPage = userRepository.findAll(spec, pageable);

        return userPage.map(user -> new UserListDTO(
            user.getId(),
            user.getUsername(),
            user.getFullName(),
            user.getPhoneNumber()
        ));
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
}