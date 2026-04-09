package com.retailstore.integration.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailstore.auth.dto.AuthLoginRequestDTO;
import com.retailstore.auth.dto.RefreshTokenRequestDTO;
import com.retailstore.integration.BaseIntegrationTest;
import com.retailstore.testdata.UserTestData;
import com.retailstore.user.entity.User;
import com.retailstore.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerIT extends BaseIntegrationTest {

    @Autowired private UserTestData userTestData;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private UserRepository userRepository;

    @BeforeEach
    void setUp(){
        userRepository.deleteAll();
    }

    //==============<< Helpers >>=================
    private User createTestUser(){
        return userTestData.createCustomer();
    }

    private AuthLoginRequestDTO buildLogin(User user){
        AuthLoginRequestDTO dto = new AuthLoginRequestDTO();
        dto.setEmail(user.getEmail());
        dto.setPassword("Pass1234");

        return dto;
    }

    private JsonNode performLogin(User user) throws Exception{
        AuthLoginRequestDTO loginRequest = buildLogin(user);

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(loginResponse);
    }

    //==============<< Login >>=================
    @Test
    @DisplayName("should login successfully and return JWT + RefreshToken")
    void shouldRegisterUser() throws Exception{
        User savedUser = createTestUser();
        performLogin(savedUser);
    }

    @Test
    @DisplayName("should fail login with invalid credentials")
    void shouldRejectInvalidLogin() throws Exception{
        AuthLoginRequestDTO login = new AuthLoginRequestDTO();
        login.setEmail("invalid@example.com");
        login.setPassword("wrong");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("Invalid")));
    }

    //==============<< Refresh Token >>=================
    @Test
    @DisplayName("should refresh token successfully")
    void shouldRefreshTokenSuccessfully() throws Exception{
        User savedUser = createTestUser();
        JsonNode loginResponse = performLogin(savedUser);

        String refreshToken = loginResponse.get("refreshToken").asText();

        RefreshTokenRequestDTO tokenRequest = new RefreshTokenRequestDTO();
        tokenRequest.setRefreshToken(refreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()));
    }

    @Test
    @DisplayName("should fail refresh token if invalid")
    void shouldRejectInvalidRefresh() throws Exception{
        RefreshTokenRequestDTO tokenRequest = new RefreshTokenRequestDTO();
        tokenRequest.setRefreshToken("bad-token");

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Invalid")));
    }

    //==============<< Logout >>=================
    @Test
    @DisplayName("should logout successfully when valid refresh token provided")
    void shouldLogoutSuccessfully() throws Exception{
        User savedUser = createTestUser();
        JsonNode loginResponse = performLogin(savedUser);

        String jwt = loginResponse.get("accessToken").asText();

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("should reject logout when access token is missing")
    void shouldRejectLogout_whenTokenMissing() throws Exception{
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", notNullValue()));
    }
}














