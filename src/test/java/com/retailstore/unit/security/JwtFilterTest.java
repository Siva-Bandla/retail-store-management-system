package com.retailstore.unit.security;

import com.retailstore.security.filter.JwtFilter;
import com.retailstore.security.jwt.JwtUtil;
import com.retailstore.security.repository.BlacklistedTokenRepository;
import com.retailstore.security.userdetails.CustomUserDetails;
import com.retailstore.security.userdetails.CustomUserDetailsService;
import com.retailstore.user.entity.User;
import com.retailstore.user.enums.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtFilterTest {

    @Mock private JwtUtil jwtUtil;
    @Mock private BlacklistedTokenRepository blacklistedTokenRepository;
    @Mock private CustomUserDetailsService customUserDetailsService;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private JwtFilter jwtFilter;

    @AfterEach
    void clearContext(){
        SecurityContextHolder.clearContext();
    }

    private User buildUser(){
        User user = new User();
        user.setId(2L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setAccountLocked(false);
        user.setRole(UserRole.ROLE_ADMIN);

        return user;
    }

    @Test
    void shouldSetAuthentication_whenTokenIsValidAndNotBlackListed() throws Exception{
        String token = "valid-token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        when(blacklistedTokenRepository.existsByToken(token)).thenReturn(false);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractEmail(token)).thenReturn("test@example.com");

        CustomUserDetails customUserDetails = new CustomUserDetails(buildUser());
        when(customUserDetailsService.loadUserByUsername("test@example.com")).thenReturn(customUserDetails);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("test@example.com", SecurityContextHolder.getContext().getAuthentication().getName());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticate_whenTokenIsBlackListed() throws Exception{
        when(request.getHeader("Authorization")).thenReturn("Bearer blacklisted-token");

        when(blacklistedTokenRepository.existsByToken("blacklisted-token")).thenReturn(true);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticate_whenTokenIsInvalid() throws Exception{
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");

        when(blacklistedTokenRepository.existsByToken("invalid-token")).thenReturn(false);

        when(jwtUtil.validateToken("invalid-token")).thenReturn(false);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticate_whenAuthenticateHeaderIsMissing() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticate_whenHeaderDoesNotStartWithBearer() throws Exception{
        when(request.getHeader("Authorization")).thenReturn("Token xyz123");

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotOverrideExistingAuthentication() throws Exception{
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("existingUser", null, Collections.emptyList())
        );

        when(request.getHeader("Authorization")).thenReturn("Bearer some-token");
        when(blacklistedTokenRepository.existsByToken("some-token")).thenReturn(false);
        when(jwtUtil.validateToken("some-token")).thenReturn(true);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertEquals("existingUser", SecurityContextHolder.getContext().getAuthentication().getName());

        verify(filterChain).doFilter(request, response);
    }
}
