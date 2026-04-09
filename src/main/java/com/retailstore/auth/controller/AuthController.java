package com.retailstore.auth.controller;

import com.retailstore.auth.dto.AuthLoginRequestDTO;
import com.retailstore.auth.dto.AuthResponseDTO;
import com.retailstore.auth.dto.RefreshTokenRequestDTO;
import com.retailstore.auth.service.AuthService;
import com.retailstore.exception.ResourceNotFoundException;
import com.retailstore.security.entity.BlacklistedToken;
import com.retailstore.security.entity.RefreshToken;
import com.retailstore.security.jwt.JwtUtil;
import com.retailstore.security.repository.BlacklistedTokenRepository;
import com.retailstore.security.repository.RefreshTokenRepository;
import com.retailstore.security.service.RefreshTokenService;
import com.retailstore.user.entity.User;
import com.retailstore.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public AuthController(AuthService authService, RefreshTokenService refreshTokenService, JwtUtil jwtUtil,
                          BlacklistedTokenRepository blacklistedTokenRepository,
                          UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.jwtUtil = jwtUtil;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody AuthLoginRequestDTO request){

        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponseDTO refresh(@RequestBody RefreshTokenRequestDTO request){

        String refreshToken = request.getRefreshToken();

        RefreshToken token = refreshTokenService.validate(refreshToken);

        User user = userRepository.findByEmail(token.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<String> roles = List.of(user.getRole().name());

        String newAccessToken = jwtUtil.generateToken(token.getUsername(), roles);

        return new AuthResponseDTO(newAccessToken, refreshToken);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    @Transactional
    public void logout(HttpServletRequest request){

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")){
            String token = authHeader.substring(7);
            String email = jwtUtil.extractEmail(token);

            BlacklistedToken blacklistedToken = new BlacklistedToken();
            blacklistedToken.setToken(token);

            blacklistedTokenRepository.save(blacklistedToken);

            List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByUsername(email);
            refreshTokens.forEach(t -> t.setRevoked(true));
            refreshTokenRepository.saveAll(refreshTokens);
        }
    }
}
