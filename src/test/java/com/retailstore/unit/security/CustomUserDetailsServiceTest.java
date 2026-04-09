package com.retailstore.unit.security;

import com.retailstore.security.userdetails.CustomUserDetails;
import com.retailstore.security.userdetails.CustomUserDetailsService;
import com.retailstore.user.entity.User;
import com.retailstore.user.enums.UserRole;
import com.retailstore.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User user;

    @BeforeEach
    void setUp(){
        user = new User();
        user.setId(2L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setAccountLocked(false);
        user.setRole(UserRole.ROLE_ADMIN);
    }

    @Test
    void loadUserByUsername_shouldReturnCustomUserDetails(){
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        CustomUserDetails customUserDetails =
                (CustomUserDetails) customUserDetailsService.loadUserByUsername("test@example.com");

        assertNotNull(customUserDetails);
        assertEquals("test@example.com", customUserDetails.getUsername());
        assertEquals("encodedPassword", customUserDetails.getPassword());
        assertEquals("ROLE_ADMIN", customUserDetails.getAuthorities().iterator().next().getAuthority());
        assertTrue(customUserDetails.isEnabled());
        assertTrue(customUserDetails.isAccountNonLocked());
        assertTrue(customUserDetails.isCredentialsNonExpired());
        assertTrue(customUserDetails.isAccountNonExpired());

        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserNotFound(){
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("missing@example.com"));

        verify(userRepository).findByEmail("missing@example.com");
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails_withLockedAccount(){
        user.setAccountLocked(true);

        when(userRepository.findByEmail("locked@example.com")).thenReturn(Optional.of(user));

        CustomUserDetails customUserDetails =
                (CustomUserDetails) customUserDetailsService.loadUserByUsername("locked@example.com");

        assertFalse(customUserDetails.isAccountNonLocked());
    }
}
