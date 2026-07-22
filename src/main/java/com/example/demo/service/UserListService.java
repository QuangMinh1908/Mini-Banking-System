package com.example.demo.service;

import com.example.demo.config.security.UserListDTO;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.config.security.UserDetailDTO; 
import com.example.demo.config.security.AccountDetailDTO;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.model.UserUpdateRequest;
import com.example.demo.repository.UserUpdateRequestRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors; 

@Service
public class UserListService {

    private final UserRepository userRepository;
    private final UserUpdateRequestRepository requestRepository;

    UserListService(UserRepository userRepository, UserUpdateRequestRepository userUpdateRequestRepository) {
        this.userRepository = userRepository;
        this.requestRepository = userUpdateRequestRepository;
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

    public void createUpdateRequest(User updatedUser) {
        User existingUser = userRepository.findById(updatedUser.getId()).orElse(null);
        if (existingUser != null) {
            UserUpdateRequest request = new UserUpdateRequest();
            request.setUser(existingUser);
            request.setNewFullName(updatedUser.getFullName());
            request.setNewPhoneNumber(updatedUser.getPhoneNumber());
            
            requestRepository.save(request);
        }
    }

    // Detail user and accounts
    @Transactional(readOnly = true)
    public UserDetailDTO getUserDetailById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return null;
        }

        List<AccountDetailDTO> accountDTOs = user.getAccounts().stream()
                .map(acc -> new AccountDetailDTO(acc.getAccountNumber(), acc.getDateOpen()))
                .collect(Collectors.toList());

        return new UserDetailDTO(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getAddress(),
                user.getGender(),
                user.getCreatedAt(),
                accountDTOs
        );
    }
}