package com.retailstore.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailstore.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Handles authorization failures (HTTP 403 - Forbidden).
 *
 * <p>This handler is invoked when an authenticated user attempts to access
 * a resource for which they do not have sufficient permissions.</p>
 *
 * <p>Typical scenarios include:</p>
 * <ul>
 *     <li>User lacks required role (e.g., accessing ADMIN endpoint as CUSTOMER)</li>
 *     <li>Access restrictions defined via {@code @PreAuthorize} or security configuration</li>
 * </ul>
 *
 * <p>It returns a structured JSON response using {@link ApiError}, ensuring
 * consistency across the application.</p>
 *
 * <p>This is part of the Spring Security exception handling mechanism and
 * executes before the request reaches the controller.</p>
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Autowired
    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Handles access denied exceptions.
     *
     * <p>This method is called when an authenticated user tries to access
     * a resource without the necessary permissions.</p>
     *
     * @param request                  the HTTP request that resulted in an access denied exception
     * @param response                 the HTTP response to which the error will be written
     * @param accessDeniedException    the exception that caused the access denial
     * @throws IOException             if an input or output exception occurs
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        ApiError error = ApiError.builder()
                .timeStamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message("Access denied: You do not have permission to access this resource.")
                .path(request.getRequestURI())
                .build();

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
