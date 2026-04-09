package com.retailstore.unit.security;

import com.retailstore.exception.ResourceConflictException;
import com.retailstore.exception.ResourceNotFoundException;
import com.retailstore.exception.UnauthorizedException;
import com.retailstore.security.entity.RefreshToken;
import com.retailstore.security.repository.RefreshTokenRepository;
import com.retailstore.security.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    //============<< HELPER >>===============
    private RefreshToken buildRefreshToken(String token, boolean expired){
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUsername("john@example.com");
        refreshToken.setExpiryDate(
                expired ? LocalDateTime.now().minusMinutes(10)
                        : LocalDateTime.now().plusMinutes(10)
        );

        return refreshToken;
    }

    //============<< HELPER >>===============
    @Test
    void shouldCreateRefreshTokenSuccessfully(){
        String username = "john@example.com";
        String token = refreshTokenService.createRefreshToken(username);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertDoesNotThrow(() -> UUID.fromString(token));

        verify(refreshTokenRepository).deleteByUsername(username);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void shouldValidateRefreshTokenSuccessfully(){
        RefreshToken token = buildRefreshToken("valid-token", false);

        when(refreshTokenRepository.findByToken("valid-token"))
                .thenReturn(Optional.of(token));

        RefreshToken result = refreshTokenService.validate("valid-token");

        assertNotNull(result);
        assertEquals("valid-token", result.getToken());
    }

    @Test
    void shouldThrowNotFound_whenRefreshTokenDoesNotExist(){
        when(refreshTokenRepository.findByToken("unknown-token"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> refreshTokenService.validate("unknown-token"));
    }

    @Test
    void shouldThrowExpired_whenTokenIsExpired(){
        RefreshToken token = buildRefreshToken("expired-token", true);

        when(refreshTokenRepository.findByToken("expired-token"))
                .thenReturn(Optional.of(token));

        assertThrows(UnauthorizedException.class,
                () -> refreshTokenService.validate("expired-token"));
    }
}
