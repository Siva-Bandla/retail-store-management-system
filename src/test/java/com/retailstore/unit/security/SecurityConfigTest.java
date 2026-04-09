package com.retailstore.unit.security;

import com.retailstore.security.config.SecurityConfig;
import com.retailstore.security.exception.CustomAccessDeniedHandler;
import com.retailstore.security.filter.JwtFilter;
import com.retailstore.security.userdetails.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SecurityConfigTest {

    @Mock private JwtFilter jwtFilter;
    @Mock private AuthenticationEntryPoint authenticationEntryPoint;
    @Mock private CustomAccessDeniedHandler accessDeniedHandler;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private AuthenticationConfiguration authenticationConfiguration;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private SecurityConfig securityConfig;

    //=============<< Password Encoder >>================
    @Test
    void passwordEncoderShouldReturnBcrypt(){
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        assertNotNull(encoder);
        assertEquals("org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder",
                encoder.getClass().getName());
    }

    //=============<< Authentication Manager >>================
    @Test
    void shouldReturnAuthenticationManager() throws Exception{
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

        AuthenticationManager result = securityConfig.authenticationManager(authenticationConfiguration);

        assertNotNull(result);
        assertEquals(authenticationManager, result);
        verify(authenticationConfiguration).getAuthenticationManager();
    }

    //=============<< Authentication Provider >>================
    @Test
    void authenticationProviderShouldBeDaoProvider(){
        AuthenticationProvider provider = securityConfig.authenticationProvider();

        assertNotNull(provider);
        assertInstanceOf(DaoAuthenticationProvider.class, provider);

        DaoAuthenticationProvider dao = (DaoAuthenticationProvider) provider;

        assertNotNull(dao);
    }
}
