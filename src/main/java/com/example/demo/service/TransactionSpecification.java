package com.example.demo.service;

import com.example.demo.model.Transaction;
import com.example.demo.model.Account;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public class TransactionSpecification {

    public static Specification<Transaction> isRelatedToUserId(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }

            // ÉP SỬ DỤNG LEFT JOIN ĐỂ KHÔNG BỊ MẤT GIAO DỊCH NẠP/RÚT TIỀN
            Join<Transaction, Account> fromAccountJoin = root.join("fromAccount", JoinType.LEFT);
            Join<Transaction, Account> toAccountJoin = root.join("toAccount", JoinType.LEFT);

            // Truy vấn tới user_id thông qua các bản ghi đã LEFT JOIN
            Predicate isSender = criteriaBuilder.equal(fromAccountJoin.get("user").get("id"), userId);
            Predicate isReceiver = criteriaBuilder.equal(toAccountJoin.get("user").get("id"), userId);

            return criteriaBuilder.or(isSender, isReceiver);
        };
    }
}