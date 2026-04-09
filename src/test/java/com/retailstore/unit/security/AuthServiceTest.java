package com.retailstore.unit.security;

import com.retailstore.auth.dto.AuthLoginRequestDTO;
import com.retailstore.auth.dto.AuthResponseDTO;
import com.retailstore.auth.service.AuthService;
import com.retailstore.auth.service.UserSecurityService;
import com.retailstore.exception.AccountLockedException;
import com.retailstore.security.jwt.JwtUtil;
import com.retailstore.security.service.RefreshTokenService;
import com.retailstore.security.userdetails.CustomUserDetails;
import com.retailstore.user.entity.User;
import com.retailstore.user.enums.UserRole;
import com.retailstore.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserSecurityService userSecurityService;

    @InjectMocks
    private AuthService authService;

    //================<< BUILDERS >>================
    private User buildUser(boolean locked){
        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword("encoded");
        user.setAccountLocked(locked);
        user.setFailedAttempts(0);
        user.setLockTime(locked ? LocalDateTime.now() : null);

        return user;
    }

    private AuthLoginRequestDTO buildLoginRequest(){
        AuthLoginRequestDTO request = new AuthLoginRequestDTO();
        request.setEmail("john@example.com");
        request.setPassword("1234");

        return request;
    }

    private CustomUserDetails buildCUD(User user) {
        return new CustomUserDetails(user);
    }
    private Authentication buildAuthentication(CustomUserDetails cud){

        return new UsernamePasswordAuthenticationToken(cud, null, cud.getAuthorities());
    }

    // ============ SUCCESS CASE ============
    @Test
    void shouldLoginSuccessfully(){
        User user = buildUser(false);
        user.setRole(UserRole.ROLE_CUSTOMER);

        CustomUserDetails cud = buildCUD(user);
        Authentication auth = buildAuthentication(cud);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        when(jwtUtil.generateToken(any(), any())).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn("refresh-token");

        AuthResponseDTO response = authService.login(buildLoginRequest());

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        verify(userRepository).save(user);//unlock
    }

    // ============ USER NOT FOUND ============
    @Test
    void shouldThrowBadCredentials_whenUserNotFound(){
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class,
                () -> authService.login(buildLoginRequest()));
    }

    // ============ ACCOUNT STILL LOCKED ============
    @Test
    void shouldThrowAccountLocked_whenLockNotExpired(){
        User user = buildUser(true);
        user.setLockTime(LocalDateTime.now().plusMinutes(10));

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThrows(AccountLockedException.class,
                () -> authService.login(buildLoginRequest()));
    }

    // ============ UNLOCK IF LOCK EXPIRED ============
    @Test
    void shouldUnlockUser_whenLockExpired(){
        User user = buildUser(true);
        user.setLockTime(LocalDateTime.now().minusMinutes(10));//lock expired
        user.setRole(UserRole.ROLE_CUSTOMER);

        CustomUserDetails cud = buildCUD(user);
        Authentication auth = buildAuthentication(cud);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        when(jwtUtil.generateToken(any(), any())).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn("refresh-token");

        AuthResponseDTO response = authService.login(buildLoginRequest());

        assertNotNull(response);
        verify(userRepository, times(2)).save(any());
        assertFalse(user.isAccountLocked());
    }

    // ============ BAD CREDENTIALS DURING AUTHENTICATION ============
    @Test
    void shouldIncreaseFailedAttempts_whenBadCredentials(){
        User user = buildUser(false);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(buildLoginRequest()));

        verify(userSecurityService).handleFailedLogin(user, "john@example.com");
    }

    @Test
    void shouldThrowAccountLocked_whenSpringSecurityLockedException(){
        User user = buildUser(false);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        when(authenticationManager.authenticate(any())).thenThrow(new LockedException("locked"));

        assertThrows(AccountLockedException.class,
                () -> authService.login(buildLoginRequest()));
    }
}
