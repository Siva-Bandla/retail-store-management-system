package com.retailstore.unit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailstore.exception.ApiError;
import com.retailstore.security.exception.CustomAccessDeniedHandler;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomAccessDeniedHandlerTest {

    private CustomAccessDeniedHandler handler;
    private ObjectMapper objectMapper;

    private HttpServletRequest request;
    private HttpServletResponse response;

    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() throws IOException{
        objectMapper = spy(new ObjectMapper());
        objectMapper.findAndRegisterModules();

        handler = new CustomAccessDeniedHandler(objectMapper);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        outputStream = new ByteArrayOutputStream();

        ServletOutputStream servletOut = new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) {
                outputStream.write(b);
            }
        };

        when(response.getOutputStream()).thenReturn(servletOut);
        when(request.getRequestURI()).thenReturn("api/test");
    }

    @Test
    void shouldReturnForbiddenErrorResponse() throws Exception{
        AccessDeniedException ex = new AccessDeniedException("Forbidden");
        handler.handle(request, response, ex);

        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);

        ArgumentCaptor<ApiError> captor = ArgumentCaptor.forClass(ApiError.class);

        verify(objectMapper).writeValue(any(ServletOutputStream.class), captor.capture());

        ApiError capturedError = captor.getValue();

        assertNotNull(capturedError);
        assertEquals(HttpStatus.FORBIDDEN.value(), capturedError.getStatus());
        assertEquals("Forbidden", capturedError.getError());
        assertEquals("Access denied: You do not have permission to access this resource.",
                capturedError.getMessage());
        assertEquals("api/test", capturedError.getPath());
        assertNotNull(capturedError.getTimeStamp());

        String json = outputStream.toString();
        assertFalse(json.isEmpty());
        assertTrue(json.contains("\"status\":403"));
    }
}
