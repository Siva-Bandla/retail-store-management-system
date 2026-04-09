package com.retailstore.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailstore.auth.dto.AuthLoginRequestDTO;
import com.retailstore.category.entity.Category;
import com.retailstore.integration.BaseIntegrationTest;
import com.retailstore.inventory.dto.InventoryRequestDTO;
import com.retailstore.inventory.entity.Inventory;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class InventoryControllerIT extends BaseIntegrationTest {

    @Autowired private UserTestData userTestData;
    @Autowired private CategoryTestData categoryTestData;
    @Autowired private ProductTestData productTestData;
    @Autowired private InventoryTestData inventoryTestData;
    @Autowired private ObjectMapper objectMapper;

    private User admin;
    private User customer;
    private Product savedProduct;
    private Inventory savedInventory;

    @BeforeEach
    void setup() {
        admin = userTestData.createAdmin();
        customer = userTestData.createCustomer();

        Category savedCategory = categoryTestData.createCategory("Electronics");
        savedProduct = productTestData.createProduct(savedCategory.getId(), 50000);
    }

    //====================<< LOGIN HELPERS >>====================
    private String loginJson(User user) throws Exception {
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
        String json = loginJson(user);
        return "Bearer " + objectMapper.readTree(json).get("accessToken").asText();
    }

    private InventoryRequestDTO buildRequest() {
        InventoryRequestDTO dto = new InventoryRequestDTO();
        dto.setProductId(savedProduct.getId());
        dto.setStock(5);
        return dto;
    }

    private Inventory createInventory(){
        return inventoryTestData.createInventory(savedProduct.getId(), 10);
    }

    //====================<< CREATE INVENTORY >>====================
    @Test
    @DisplayName("Admin can create inventory")
    void adminCanCreateInventory() throws Exception {

        InventoryRequestDTO req = new InventoryRequestDTO();
        req.setProductId(savedProduct.getId());
        req.setStock(20);

        mockMvc.perform(post("/inventories")
                        .header("Authorization", token(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId", is(savedProduct.getId().intValue())))
                .andExpect(jsonPath("$.stock", is(20)));
    }

    @Test
    @DisplayName("Customer cannot create inventory")
    void customerCannotCreate() throws Exception {

        InventoryRequestDTO req = buildRequest();

        mockMvc.perform(post("/inventories")
                        .header("Authorization", token(customer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    //====================<< UPDATE INVENTORY >>====================
    @Test
    @DisplayName("Admin can update inventory")
    void adminCanUpdateInventory() throws Exception {
        savedInventory = createInventory();

        InventoryRequestDTO req = new InventoryRequestDTO();
        req.setProductId(savedProduct.getId());
        req.setStock(5); // add 5 → total becomes 10 + 5 = 15

        mockMvc.perform(put("/inventories/" + savedInventory.getId())
                        .header("Authorization", token(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock", is(15)));
    }

    @Test
    @DisplayName("Customer cannot update inventory")
    void customerCannotUpdateInventory() throws Exception {
        savedInventory = createInventory();

        InventoryRequestDTO req = buildRequest();

        mockMvc.perform(put("/inventories/" + savedInventory.getId())
                        .header("Authorization", token(customer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    //====================<< GET ALL INVENTORIES >>====================
    @Test
    @DisplayName("Admin can get all inventories")
    void adminCanGetAllInventories() throws Exception {
        savedInventory = createInventory();

        inventoryTestData.createInventory(savedProduct.getId() + 1, 30);

        mockMvc.perform(get("/inventories")
                        .header("Authorization", token(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Customer cannot get inventories")
    void customerCannotGetAllInventories() throws Exception {

        mockMvc.perform(get("/inventories")
                        .header("Authorization", token(customer)))
                .andExpect(status().isForbidden());
    }

    //====================<< GET INVENTORY BY PRODUCT ID >>====================
    @Test
    @DisplayName("Admin can get inventory by product id")
    void adminCanGetInventoryByProductId() throws Exception {
        savedInventory = createInventory();

        mockMvc.perform(get("/inventories/" + savedProduct.getId())
                        .header("Authorization", token(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId", is(savedProduct.getId().intValue())));
    }

    @Test
    @DisplayName("Customer cannot get inventory by product id")
    void customerCannotGetInventoryByProductId() throws Exception {
        savedInventory = createInventory();

        mockMvc.perform(get("/inventories/" + savedProduct.getId())
                        .header("Authorization", token(customer)))
                .andExpect(status().isForbidden());
    }

    //====================<< DELETE INVENTORY (DEACTIVATE) >>====================
    @Test
    @DisplayName("Admin can deactivate inventory")
    void adminCanDeactivateInventory() throws Exception {
        savedInventory = inventoryTestData.createInventory(savedProduct.getId() + 1, 30);

        mockMvc.perform(delete("/inventories/" + savedInventory.getId())
                        .header("Authorization", token(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted", is(true)));
    }

    @Test
    @DisplayName("Customer cannot deactivate inventory")
    void customerCannotDeactivate() throws Exception {
        savedInventory = createInventory();

        mockMvc.perform(delete("/inventories/" + savedInventory.getId())
                        .header("Authorization", token(customer)))
                .andExpect(status().isForbidden());
    }
}