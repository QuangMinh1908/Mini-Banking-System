package com.example.demo.repository;

import com.example.demo.model.UserUpdateRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserUpdateRequestRepository extends JpaRepository<UserUpdateRequest, Long> {
}