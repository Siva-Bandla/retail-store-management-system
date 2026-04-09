package com.retailstore.integration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailstore.auth.dto.AuthLoginRequestDTO;
import com.retailstore.auth.dto.AuthResponseDTO;
import com.retailstore.integration.BaseIntegrationTest;
import com.retailstore.user.dto.UserRegisterRequestDTO;
import com.retailstore.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthenticationFlowIT extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullAuthenticationFlow() throws Exception{
        //Register
        UserRegisterRequestDTO registerRequest = new UserRegisterRequestDTO();
        registerRequest.setEmail("flow@example.com");
        registerRequest.setPassword("Pass1234");
        registerRequest.setName("Flow Test");
        registerRequest.setRole(UserRole.ROLE_ADMIN);

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        //Login
        AuthLoginRequestDTO loginRequest = new AuthLoginRequestDTO();
        loginRequest.setEmail("flow@example.com");
        loginRequest.setPassword("Pass1234");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthResponseDTO authResponse = objectMapper.readValue(loginResponse, AuthResponseDTO.class);

        assertThat(authResponse.getRefreshToken()).isNotBlank();
        assertThat(authResponse.getRefreshToken()).isNotBlank();

        String jwt = authResponse.getAccessToken();
        String refreshToken = authResponse.getRefreshToken();

        //Access protected endpoint using JWT
        mockMvc.perform(get("/users")
                    .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk());

        //Refresh Token -> New JWT
        String refreshResponse = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthResponseDTO refreshed = objectMapper.readValue(refreshResponse, AuthResponseDTO.class);

        assertThat(refreshed.getAccessToken()).isNotBlank();

        String newJwt = refreshed.getAccessToken();

        //Logout (JWT blacklisted)
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + newJwt))
                .andExpect(status().isOk());

        //Try accessing protected endpoint again
        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + newJwt))
                .andExpect(status().isUnauthorized());
    }
}
