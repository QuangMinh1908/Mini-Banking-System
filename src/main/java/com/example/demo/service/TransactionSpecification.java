package com.example.demo.service;

import com.example.demo.model.Transaction;
import com.example.demo.model.Account;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.time.LocalDateTime;

public class TransactionSpecification {

    public static Specification<Transaction> isRelatedToUserIdWithCursor(Long userId, LocalDateTime lastDate, Long lastId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.disjunction();
            }

            Join<Transaction, Account> fromAccountJoin = root.join("fromAccount", JoinType.LEFT);
            Join<Transaction, Account> toAccountJoin = root.join("toAccount", JoinType.LEFT);

            Predicate isSender = criteriaBuilder.equal(fromAccountJoin.get("user").get("id"), userId);
            Predicate isReceiver = criteriaBuilder.equal(toAccountJoin.get("user").get("id"), userId);
            Predicate userCondition = criteriaBuilder.or(isSender, isReceiver);

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