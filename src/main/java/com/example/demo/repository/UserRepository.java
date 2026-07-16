package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // fix username thành chữ thường, thêm các field khác
        @Query("SELECT u FROM User u WHERE (LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR u.phoneNumber LIKE CONCAT('%', :keyword, '%') " +
           "OR CAST(u.id AS string) LIKE CONCAT('%', :keyword, '%')) " +
           "AND LOWER(u.role) != 'admin'")
    List<User> searchCustomers(@Param("keyword") String keyword);

    List<User> findByRoleNot(String role);
    Optional<User> findByUsername(String username);
}

