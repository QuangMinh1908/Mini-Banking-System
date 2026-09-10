package com.example.demo.repository;

import com.example.demo.model.Transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    @Override
    @EntityGraph(attributePaths = {"account"})
    Page<Transaction> findAll(Specification<Transaction> spec, Pageable pageable);
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.account.id = :accountId AND t.direction = 'DEBIT' AND t.transactionDate >= :startOfDay AND t.transactionDate <= :endOfDay")
    BigDecimal sumOutgoingAmountByAccountIdAndDate(
            @Param("accountId") Long accountId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.account.user.id = :userId AND t.isRead = false")
    long countUnreadByUserId(@Param("userId") Long userId);
    @Modifying
    @Transactional
    @Query("UPDATE Transaction t SET t.isRead = true WHERE t.account.id IN " +
           "(SELECT a.id FROM Account a WHERE a.user.id = :userId) AND t.isRead = false")
    void markAllAsReadByUserId(@Param("userId") Long userId);
}