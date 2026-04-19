package com.retailstore.security.filter;

import com.retailstore.security.jwt.JwtUtil;
import com.retailstore.security.repository.BlacklistedTokenRepository;
import com.retailstore.security.userdetails.CustomUserDetails;
import com.retailstore.security.userdetails.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final CustomUserDetailsService customUserDetailsService;

    @Autowired
    public JwtFilter(JwtUtil jwtUtil,
                     BlacklistedTokenRepository blacklistedTokenRepository, CustomUserDetailsService customUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip JWT filter for preflight requests
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    /**
     * Filters incoming HTTP requests to validate JWT tokens.
     *
     * <p>This method extracts the Authorization header, validates the JWT token,
     * and sets authentication in the SecurityContext if the token is valid.
     * If no token is present or validation fails, the request proceeds without authentication.</p>
     *
     * @param request the incoming HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain to pass the request and response
     *
     * @throws ServletException if an exception occurs during filtering
     * @throws IOException if an I/O error occurs during filtering
     */
    @Override
    public void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")){

            String token = authHeader.substring(7);

            if (!blacklistedTokenRepository.existsByToken(token)
                    && jwtUtil.validateToken(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null){

                String username = jwtUtil.extractEmail(token);
                CustomUserDetails customUserDetails =
                        (CustomUserDetails) customUserDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                customUserDetails, null, customUserDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
