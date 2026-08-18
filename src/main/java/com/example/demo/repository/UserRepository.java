package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByUsername(String username);
    @EntityGraph(attributePaths = {"accounts"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithAccounts(@Param("id") Long id);
    boolean existsByPhoneNumber(String phoneNumber);
}