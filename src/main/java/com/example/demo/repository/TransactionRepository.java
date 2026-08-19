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
}