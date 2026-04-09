package com.retailstore.integration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailstore.integration.BaseIntegrationTest;
import com.retailstore.testdata.CartTestData;
import com.retailstore.testdata.UserTestData;
import com.retailstore.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JWTFilterIT extends BaseIntegrationTest {

    @Autowired private UserTestData userTestData;
    @Autowired private CartTestData cartTestData;

    @Autowired
    private ObjectMapper objectMapper;

    private User savedCustomer;

    private String jwt;

    @BeforeEach
    void setUp() throws Exception{
        savedCustomer = userTestData.createCustomer();
        cartTestData.createCart(savedCustomer.getId());

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
    }


    @Test
    void shouldRejectMissingToken() throws Exception{
        mockMvc.perform(get("/carts/user/%d".formatted(savedCustomer.getId())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectMalformedToken() throws Exception{
        mockMvc.perform(get("/carts/user/%d".formatted(savedCustomer.getId()))
                        .header("Authorization", "Bearer abc.def"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectTamperedToken() throws Exception{
        String tampered = jwt.substring(0, jwt.length() - 2) + "xx";

        mockMvc.perform(get("/carts/user/%d".formatted(savedCustomer.getId()))
                        .header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectBlacklistedToken() throws Exception{
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk());

        mockMvc.perform(get("/carts/user/%d".formatted(savedCustomer.getId()))
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowValidToken() throws Exception{
        mockMvc.perform(get("/carts/user/%d".formatted(savedCustomer.getId()))
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk());
    }
}
