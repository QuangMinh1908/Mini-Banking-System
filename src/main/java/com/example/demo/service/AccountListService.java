package com.example.demo.service;

import com.example.demo.model.Account;

import org.springframework.data.jpa.domain.Specification;

public class AccountListService {

    // Lọc theo Số tài khoản
    public static Specification<Account> hasAccountNumber(String accountNumber) {
        return (root, query, criteriaBuilder) -> {
            if (accountNumber == null || accountNumber.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            return criteriaBuilder.equal(root.get("accountNumber"), accountNumber.trim());
        };
    }

    // Lọc theo Họ Tên của chủ tài khoản
    public static Specification<Account> hasFullName(String fullName) {
        return (root, query, criteriaBuilder) -> {
            if (fullName == null || fullName.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("fullName")), "%" + fullName.toLowerCase() + "%");
        };
    }

    // Lọc theo vai trò của người dùng (ko hiển thị admin)
    public static Specification<Account> hasUserRole(String role) {
        return (root, query, criteriaBuilder) -> {
            if (role == null || role.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("user").get("role"), role);
        };
    }
}