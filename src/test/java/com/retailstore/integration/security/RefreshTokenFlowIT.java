package com.retailstore.integration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailstore.integration.BaseIntegrationTest;
import com.retailstore.testdata.UserTestData;
import com.retailstore.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RefreshTokenFlowIT extends BaseIntegrationTest {

    @Autowired private UserTestData userTestData;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String refreshToken;
    private String jwt;

    @BeforeEach
    void setUp() throws Exception{
        User savedCustomer = userTestData.createCustomer();

        String customerLoginJson = """
                {
                    "email": "%s",
                    "password": "Pass1234"
                }""".formatted(savedCustomer.getEmail());

        String customerLoginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerLoginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        jwt = objectMapper.readTree(customerLoginResponse).get("accessToken").asText();
        refreshToken = objectMapper.readTree(customerLoginResponse).get("refreshToken").asText();
    }

    @Test
    void shouldGenerateNewJwtValidRefreshToken() throws Exception{
        String body = """
                {"refreshToken": "%s"}""".formatted(refreshToken);

        String response = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String newJwt = objectMapper.readTree(response).get("accessToken").asText();
        assertNotEquals(jwt, newJwt);
    }

    @Test
    void shouldRejectInvalidRefreshToken() throws Exception{
        String body = """
                {"refreshToken": "invalid-token"}""";

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectExpiredOrBlacklistedRefreshToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                    .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk());

        String body = """
                {"refreshToken": "%s"}""".formatted(refreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}
