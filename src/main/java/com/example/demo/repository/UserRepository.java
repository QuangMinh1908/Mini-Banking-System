package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // fix username thành chữ thường, thêm các field khác
    @Query("SELECT u FROM User u WHERE u.role != 'admin' AND " +
           "(:id IS NULL OR u.id = :id) AND " +
           "(:name IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:phone IS NULL OR u.phoneNumber LIKE CONCAT('%', :phone, '%'))")
    List<User> searchUsers(@Param("id") Long id, 
                                       @Param("name") String name, 
                                       @Param("phone") String phone);

    List<User> findByRoleNot(String role);
    Optional<User> findByUsername(String username);
}

