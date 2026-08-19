package com.example.demo.service;

import com.example.demo.model.Transaction;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;

public class TransactionSpecification {

    public static Specification<Transaction> isRelatedToUserIdWithCursor(Long userId, LocalDateTime lastDate, Long lastId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.disjunction();
            }

            Predicate userCondition = criteriaBuilder.equal(root.get("account").get("user").get("id"), userId);

            if (lastDate != null && lastId != null) {
                Predicate dateLess = criteriaBuilder.lessThan(root.get("transactionDate"), lastDate);
                Predicate dateEqual = criteriaBuilder.equal(root.get("transactionDate"), lastDate);
                Predicate idLess = criteriaBuilder.lessThan(root.get("id"), lastId);
                Predicate dateEqualAndIdLess = criteriaBuilder.and(dateEqual, idLess);

                Predicate cursorCondition = criteriaBuilder.or(dateLess, dateEqualAndIdLess);
                return criteriaBuilder.and(userCondition, cursorCondition);
            }

            return userCondition;
        };
    }
}