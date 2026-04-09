package com.retailstore.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailstore.auth.dto.AuthLoginRequestDTO;
import com.retailstore.category.entity.Category;
import com.retailstore.integration.BaseIntegrationTest;
import com.retailstore.product.dto.ProductRequestDTO;
import com.retailstore.product.entity.Product;
import com.retailstore.testdata.CategoryTestData;
import com.retailstore.testdata.InventoryTestData;
import com.retailstore.testdata.ProductTestData;
import com.retailstore.testdata.UserTestData;
import com.retailstore.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductControllerIT extends BaseIntegrationTest {

    @Autowired
    private UserTestData userTestData;
    @Autowired private CategoryTestData categoryTestData;
    @Autowired private ProductTestData productTestData;
    @Autowired private InventoryTestData inventoryTestData;
    @Autowired private ObjectMapper objectMapper;

    private User savedCustomer;
    private Category savedCategory;
    private Product savedProduct;

    @BeforeEach
    void setUp() {
        savedCustomer = createCustomer();
        savedCategory = categoryTestData.createCategory("Electronics");
        savedProduct = createProduct();
    }

    //=================<< Helpers >>====================
    private User createCustomer(){
        return userTestData.createCustomer();
    }

    private User createAdmin(){
        return userTestData.createAdmin();
    }

    private Product createProduct(){
        Product product = productTestData.createProduct(savedCategory.getId(), 100);
        inventoryTestData.createInventory(product.getId(), 12);

        return product;
    }

    private ProductRequestDTO buildProductRequest(){
        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("Test Product " + System.nanoTime());
        request.setDescription("Sample product");
        request.setPrice(BigDecimal.valueOf(100));
        request.setCategoryId(savedCategory.getId());
        request.setQuantity(30);

        return request;
    }

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

    private String extractAccessToken(String json) throws Exception{
        return objectMapper.readTree(json).get("accessToken").asText();
    }

    private String authHeader(String token){
        return "Bearer " + token;
    }

    //=================<< Create Product >>====================
    @Test
    @DisplayName("Admin can create product")
    void adminCanCreateProduct() throws Exception{
        User admin = createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractAccessToken(loginResponse);

        ProductRequestDTO request = buildProductRequest();

        mockMvc.perform(post("/products")
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId", is(request.getCategoryId().intValue())))
                .andExpect(jsonPath("$.quantity", is(request.getQuantity())));
    }

    @Test
    @DisplayName("Customer cannot create product")
    void customerCannotCreateProduct() throws Exception{
        String loginResponse = login(savedCustomer);
        String accessToken = extractAccessToken(loginResponse);

        ProductRequestDTO request = buildProductRequest();

        mockMvc.perform(post("/products")
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    //=================<< Get All Product >>====================
    @Test
    @DisplayName("Anyone can get all products")
    void anyoneCanGetAllProducts() throws Exception {
        createProduct();
        createProduct();

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    //=================<< Get Product By Id >>====================
    @Test
    @DisplayName("Anyone can get product by id")
    void anyoneCanGetProductById() throws Exception{

        mockMvc.perform(get("/products/" + savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId", is(savedCategory.getId().intValue())));
    }

    //=================<< Update Product >>====================
    @Test
    @DisplayName("Admin can update product")
    void adminCanUpdateProduct() throws Exception{
        User admin = createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractAccessToken(loginResponse);

        ProductRequestDTO request = buildProductRequest();

        mockMvc.perform(put("/products/" + savedProduct.getId())
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId", is(request.getCategoryId().intValue())))
                .andExpect(jsonPath("$.price", is(request.getPrice().intValue())));
    }

    @Test
    @DisplayName("Customer cannot update product")
    void customerCannotUpdateProduct() throws Exception{
        String loginResponse = login(savedCustomer);
        String accessToken = extractAccessToken(loginResponse);

        ProductRequestDTO request = buildProductRequest();

        mockMvc.perform(put("/products/" + savedProduct.getId())
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    //=================<< Delete Product >>====================
    @Test
    @DisplayName("Admin can delete product")
    void adminCanDeleteProduct() throws Exception{
        User admin = createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractAccessToken(loginResponse);

        mockMvc.perform(delete("/products/" + savedProduct.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Customer cannot delete product")
    void customerCannotDeleteProduct() throws Exception{
        String loginResponse = login(savedCustomer);
        String accessToken = extractAccessToken(loginResponse);

        mockMvc.perform(delete("/products/" + savedProduct.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isForbidden());
    }
}
