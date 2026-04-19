package com.retailstore.auth.service;

import com.retailstore.auth.dto.AuthLoginRequestDTO;
import com.retailstore.auth.dto.AuthResponseDTO;
import com.retailstore.exception.AccountLockedException;
import com.retailstore.security.jwt.JwtUtil;
import com.retailstore.security.service.RefreshTokenService;
import com.retailstore.security.userdetails.CustomUserDetails;
import com.retailstore.user.entity.User;
import com.retailstore.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service responsible for authentication operations such as login
 * and token generation.
 *
 * <p>This service validates user credentials, enforces account lock
 * policies, and generates JWT access and refresh tokens.</p>
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static final int LOCK_DURATION_MINUTES = 3;

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final UserSecurityService userSecurityService;

    @Autowired
    public AuthService(UserRepository userRepository, JwtUtil jwtUtil,
                       RefreshTokenService refreshTokenService,
                       AuthenticationManager authenticationManager, UserSecurityService userSecurityService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.authenticationManager = authenticationManager;
        this.userSecurityService = userSecurityService;
    }

    /**
     * Authenticates a user using email and password.
     *
     * <p>This method performs:</p>
     * <ul>
     *     <li>User existence validation</li>
     *     <li>Account lock check</li>
     *     <li>Password verification</li>
     *     <li>Failed attempt tracking</li>
     *     <li>JWT access & refresh token generation</li>
     * </ul>
     *
     * @param request AuthLoginRequestDTO
     * @return {@link AuthResponseDTO} containing access and refresh tokens
     *
     * @throws BadCredentialsException if user not found or credentials invalid
     * @throws AccountLockedException if account is locked due to multiple failed attempts
     */
    @Transactional
    public AuthResponseDTO login(AuthLoginRequestDTO request){

        String email = request.getEmail();
        String password = request.getPassword();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        //if account is locked
        if (user.isAccountLocked()){

            if (user.getLockTime() != null  &&
                    user.getLockTime().
                            plusMinutes(LOCK_DURATION_MINUTES).
                            isBefore(LocalDateTime.now())){

                log.info("Unlocking user account: {}", email);
                unlockUser(user);

            }else {
                throw new AccountLockedException("Account is locked. Try again later.");
            }
        }

        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            unlockUser(user);

            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

            List<String> roles = customUserDetails.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            String accessToken = jwtUtil.generateToken(user.getId(), customUserDetails.getUsername(), roles);
            String refreshToken = refreshTokenService.createRefreshToken(customUserDetails.getUsername());

            return new AuthResponseDTO(accessToken, refreshToken);

        }catch (BadCredentialsException ex){

            userSecurityService.handleFailedLogin(user, email);

            throw new BadCredentialsException("Invalid credentials");

        }catch (LockedException ex){

            throw new AccountLockedException("Account is locked. Try again later.");
        }


    }

    private void unlockUser(User user) {

        user.setFailedAttempts(0);
        user.setAccountLocked(false);
        user.setLockTime(null);

        userRepository.save(user);
    }
}
