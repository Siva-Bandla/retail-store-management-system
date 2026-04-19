package com.retailstore.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailstore.auth.dto.AuthLoginRequestDTO;
import com.retailstore.category.dto.CategoryRequestDTO;
import com.retailstore.category.entity.Category;
import com.retailstore.integration.BaseIntegrationTest;
import com.retailstore.testdata.CategoryTestData;
import com.retailstore.testdata.UserTestData;
import com.retailstore.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CategoryControllerIT extends BaseIntegrationTest {

    @Autowired private UserTestData userTestData;
    @Autowired private CategoryTestData categoryTestData;
    @Autowired private ObjectMapper objectMapper;

    private User savedCustomer;
    private User savedAdmin;
    private Category savedCategory;

    @BeforeEach
    void setUp() {
        savedAdmin = userTestData.createAdmin();
        savedCustomer = userTestData.createCustomer();
        savedCategory = categoryTestData.createCategory("Electronics");
    }

    //=================<< Helpers >>====================
    private String login(User user) throws Exception {
        AuthLoginRequestDTO login = new AuthLoginRequestDTO();
        login.setEmail(user.getEmail());
        login.setPassword("Pass1234");

        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private String token(User user) throws Exception {
        String json = login(user);
        return "Bearer " + objectMapper.readTree(json).get("accessToken").asText();
    }

    private CategoryRequestDTO buildRequest() {
        CategoryRequestDTO dto = new CategoryRequestDTO();
        dto.setName("Category " + System.nanoTime());
        dto.setDescription("Sample Description");
        return dto;
    }

    //=================<< Create Category >>====================
    @Test
    @DisplayName("Admin can create a category")
    void adminCanCreateCategory() throws Exception {

        CategoryRequestDTO req = buildRequest();

        mockMvc.perform(post("/categories")
                        .header("Authorization", token(savedAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(req.getName())));
    }

    @Test
    @DisplayName("Customer cannot create a category")
    void customerCannotCreateCategory() throws Exception {

        CategoryRequestDTO req = buildRequest();

        mockMvc.perform(post("/categories")
                        .header("Authorization", token(savedCustomer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    //=================<< Get All Categories >>====================
    @Test
    @DisplayName("Anyone can get all categories")
    void anyoneCanGetAllCategories() throws Exception {

        categoryTestData.createCategory("Sports");
        categoryTestData.createCategory("Fashion");

        mockMvc.perform(get("/categories")
                        .header("Authorization", token(savedCustomer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    //=================<< Get Category By Id >>====================
    @Test
    @DisplayName("Admin can get category by id")
    void adminCanGetCategoryById() throws Exception {

        mockMvc.perform(get("/categories/" + savedCategory.getId())
                        .header("Authorization", token(savedAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedCategory.getId().intValue())));
    }

    @Test
    @DisplayName("Customer cannot get category by id")
    void customerCannotGetCategoryById() throws Exception {

        mockMvc.perform(get("/categories/" + savedCategory.getId())
                        .header("Authorization", token(savedCustomer)))
                .andExpect(status().isForbidden());
    }

    //=================<< Update Category >>====================
    @Test
    @DisplayName("Admin can update a category")
    void adminCanUpdateCategory() throws Exception {

        CategoryRequestDTO req = buildRequest();

        mockMvc.perform(put("/categories/" + savedCategory.getId())
                        .header("Authorization", token(savedAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(req.getName())));
    }

    @Test
    @DisplayName("Customer cannot update a category")
    void customerCannotUpdateCategory() throws Exception {

        CategoryRequestDTO req = buildRequest();

        mockMvc.perform(put("/categories/" + savedCategory.getId())
                        .header("Authorization", token(savedCustomer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    //=================<< Delete Category >>====================
    @Test
    @DisplayName("Admin can delete a category")
    void adminCanDeleteCategory() throws Exception {

        mockMvc.perform(delete("/categories/" + savedCategory.getId())
                        .header("Authorization", token(savedAdmin)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Customer cannot delete a category")
    void customerCannotDeleteCategory() throws Exception {

        mockMvc.perform(delete("/categories/" + savedCategory.getId())
                        .header("Authorization", token(savedCustomer)))
                .andExpect(status().isForbidden());
    }
}