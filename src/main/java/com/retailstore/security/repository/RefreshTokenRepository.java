package com.retailstore.security.repository;

import com.retailstore.security.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUsername(String username);

    List<RefreshToken> findAllByUsername(String email);
}
