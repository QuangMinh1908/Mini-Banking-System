package com.example.demo.repository;
import java.util.List;
import com.example.demo.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByAccountName(String accountName);
    List<BankAccount> findByRole(String role);
}