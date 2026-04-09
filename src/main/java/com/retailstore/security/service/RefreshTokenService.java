package com.retailstore.security.service;

import com.retailstore.exception.ResourceNotFoundException;
import com.retailstore.exception.UnauthorizedException;
import com.retailstore.security.entity.RefreshToken;
import com.retailstore.security.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String createRefreshToken(String username){

        refreshTokenRepository.deleteByUsername(username);

        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUsername(username);
        token.setExpiryDate(LocalDateTime.now().plusDays(7));

        refreshTokenRepository.save(token);

        return token.getToken();
    }

    public RefreshToken validate(String token){

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid refresh token"));

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now()) || refreshToken.isRevoked()){
            throw new UnauthorizedException("Refresh token expired or revoked. Please login again.");
        }

        return refreshToken;
    }
}
