package com.retailstore.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailstore.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Handles authentication failures (HTTP 401 - Unauthorized).
 *
 * <p>This component is triggered when a client attempts to access a secured
 * endpoint without valid authentication credentials. Common scenarios include:</p>
 *
 * <ul>
 *     <li>Missing JWT token in the request</li>
 *     <li>Invalid or malformed JWT token</li>
 *     <li>Expired JWT token</li>
 * </ul>
 *
 * <p>Instead of returning a default HTML error page, this handler ensures a
 * consistent JSON response structure using {@link ApiError}.</p>
 *
 * <p>This class is part of the Spring Security filter chain and is invoked
 * before the request reaches the controller layer.</p>
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Autowired
    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Commences an authentication scheme.
     *
     * <p>This method is called whenever an unauthenticated user tries to access
     * a protected resource. It builds a standardized error response and writes
     * it to the HTTP response output stream.</p>
     *
     * @param request           the HTTP request that resulted in an authentication exception
     * @param response          the HTTP response to which the error will be written
     * @param authException     the exception that caused the authentication failure
     * @throws IOException      if an input or output exception occurs
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        ApiError error = ApiError.builder()
                .timeStamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message("Authentication failed. invalid or missing token.")
                .path(request.getRequestURI())
                .build();

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
