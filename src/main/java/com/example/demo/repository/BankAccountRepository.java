package com.example.demo.repository;
import java.util.List;
import com.example.demo.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByAccountName(String accountName);
    List<BankAccount> findByRole(String role);

    @Query("SELECT b FROM BankAccount b WHERE LOWER(b.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.accountName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR b.phoneNumber LIKE CONCAT('%', :keyword, '%')" +
            "OR CAST(b.id AS string) LIKE CONCAT('%', :keyword, '%') " +
            "OR LOWER(CASE " +
            "    WHEN b.id < 10 THEN CONCAT('#kh00', CAST(b.id AS string)) " +
            "    WHEN b.id < 100 THEN CONCAT('#kh0', CAST(b.id AS string)) " +
            "    ELSE CONCAT('#kh', CAST(b.id AS string)) " +
            "END) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(CASE " +
            "    WHEN b.id < 10 THEN CONCAT('kh00', CAST(b.id AS string)) " +
            "    WHEN b.id < 100 THEN CONCAT('kh0', CAST(b.id AS string)) " +
            "    ELSE CONCAT('kh', CAST(b.id AS string)) " +
            "END) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<BankAccount> searchByKeyword(@Param("keyword") String keyword);
}
