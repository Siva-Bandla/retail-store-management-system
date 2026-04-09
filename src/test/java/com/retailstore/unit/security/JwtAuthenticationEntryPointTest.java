package com.retailstore.unit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailstore.exception.ApiError;
import com.retailstore.security.exception.JwtAuthenticationEntryPoint;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationEntryPointTest {

    private JwtAuthenticationEntryPoint entryPoint;

    private ObjectMapper objectMapper;

    @BeforeEach
    void SetUp(){
        objectMapper = mock(ObjectMapper.class);
        entryPoint = new JwtAuthenticationEntryPoint(objectMapper);
    }

    @Test
    void shouldReturnUnauthorizedJsonResponse() throws Exception{
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        AuthenticationException authException = mock(AuthenticationException.class);

        when(request.getRequestURI()).thenReturn("/api/test");

        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(outputStream);

        entryPoint.commence(request, response, authException);

        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ArgumentCaptor<ApiError> captor = ArgumentCaptor.forClass(ApiError.class);

        verify(objectMapper).writeValue(eq(outputStream), captor.capture());

        ApiError error = captor.getValue();

        assertEquals(HttpStatus.UNAUTHORIZED.value(), error.getStatus());
        assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), error.getError());
        assertEquals("Authentication failed. invalid or missing token.", error.getMessage());
        assertEquals("/api/test", error.getPath());
        assertNotNull(error.getTimeStamp());
    }
}
