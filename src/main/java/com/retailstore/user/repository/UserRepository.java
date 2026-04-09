package com.retailstore.user.repository;

import com.retailstore.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(@NotBlank(message = "User name cannot be blank") @Size(max = 100, message = "User name cannot exceed 100 characters") String email);
}
