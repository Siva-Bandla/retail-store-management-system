package com.retailstore.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailstore.auth.dto.AuthLoginRequestDTO;
import com.retailstore.cart.dto.AddToCartRequestDTO;
import com.retailstore.cart.entity.Cart;
import com.retailstore.cart.entity.CartItem;
import com.retailstore.category.entity.Category;
import com.retailstore.integration.BaseIntegrationTest;
import com.retailstore.product.entity.Product;
import com.retailstore.testdata.*;
import com.retailstore.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartControllerIT extends BaseIntegrationTest {

    @Autowired private UserTestData userTestData;
    @Autowired private CategoryTestData categoryTestData;
    @Autowired private ProductTestData productTestData;
    @Autowired private InventoryTestData inventoryTestData;
    @Autowired private CartTestData cartTestData;
    @Autowired private ObjectMapper objectMapper;

    private Product savedProduct;
    private User savedCustomer;
    private Cart savedCart;

    @BeforeEach
    void setUp(){
        savedCustomer = createCustomer();
        Category savedCategory = categoryTestData.createCategory("Electronics");
        savedProduct = productTestData.createProduct(savedCategory.getId(), 100);
        inventoryTestData.createInventory(savedProduct.getId(), 15);
        savedCart = cartTestData.createCart(savedCustomer.getId());
    }

    //=================<< Helpers >>====================
    private User createCustomer(){
        return userTestData.createCustomer();
    }

    private User createAdmin(){
        return userTestData.createAdmin();
    }

    private AddToCartRequestDTO buildCartRequest(){
        AddToCartRequestDTO request = new AddToCartRequestDTO();
        request.setProductId(savedProduct.getId());
        request.setUserId(savedCustomer.getId());
        request.setQuantity(3);

        return request;
    }

    private CartItem createCartIem(){
        return cartTestData.createCartItem(savedCart.getId(), savedProduct.getId(), 3);
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

    //=================<< Add To Cart >>====================
    @Test
    @DisplayName("Customer can add items to own cart")
    void customerCanAddItemsToOwnCart() throws Exception {
        String loginResponse = login(savedCustomer);
        String accessToken = extractAccessToken(loginResponse);

        AddToCartRequestDTO request = buildCartRequest();

        mockMvc.perform(post("/carts/add")
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", is(savedCustomer.getId().intValue())))
                .andExpect(jsonPath("$.cartItems[0].productId", is(savedProduct.getId().intValue())));
    }

    @Test
    @DisplayName("Customer cannot add items to other's cart")
    void customerCannotAddItemsToOthersCart() throws Exception {
        User customer2 = createCustomer();

        String loginResponse = login(customer2);
        String accessToken = extractAccessToken(loginResponse);

        AddToCartRequestDTO request = buildCartRequest();

        mockMvc.perform(post("/carts/add")
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin cannot add items to other's cart")
    void adminCannotAddItemsToOthersCart() throws Exception {
        User admin = createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractAccessToken(loginResponse);

        AddToCartRequestDTO request = buildCartRequest();

        mockMvc.perform(post("/carts/add")
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    //=================<< Remove From Cart >>====================
    @Test
    @DisplayName("Customer can remove items from own cart")
    void customerCanRemoveItemsTFromOwnCart() throws Exception {
        String loginResponse = login(savedCustomer);
        String accessToken = extractAccessToken(loginResponse);

        CartItem savedCartItem = createCartIem();

        mockMvc.perform(delete("/carts/item/" + savedCartItem.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Customer cannot remove items from other's cart")
    void customerCannotRemoveItemsFromOthersCart() throws Exception {
        User customer2 = createCustomer();

        String loginResponse = login(customer2);
        String accessToken = extractAccessToken(loginResponse);

        CartItem savedCartItem = createCartIem();

        mockMvc.perform(delete("/carts/item/" + savedCartItem.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin cannot remove items from other's cart")
    void adminCannotRemoveItemsFromOthersCart() throws Exception {
        User admin = createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractAccessToken(loginResponse);

        CartItem savedCartItem = createCartIem();

        mockMvc.perform(delete("/carts/item/" + savedCartItem.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isForbidden());
    }

    //=================<< Get Cart By User >>====================
    @Test
    @DisplayName("Customer can get own cart")
    void customerCanGetOwnCart() throws Exception{
        String loginResponse = login(savedCustomer);
        String accessToken = extractAccessToken(loginResponse);

        createCartIem();
        createCartIem();
        createCartIem();

        mockMvc.perform(get("/carts/user/" + savedCustomer.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartItems", hasSize(3)));
    }

    @Test
    @DisplayName("Customer cannot get other's cart")
    void customerCannotGetOthersCart() throws Exception{
        User customer2 = createCustomer();

        String loginResponse = login(customer2);
        String accessToken = extractAccessToken(loginResponse);

        createCartIem();
        createCartIem();
        createCartIem();

        mockMvc.perform(get("/carts/user/" + savedCustomer.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin can get any user cart")
    void customerCanGetOthersCart() throws Exception{
        User admin = createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractAccessToken(loginResponse);

        createCartIem();
        createCartIem();
        createCartIem();

        mockMvc.perform(get("/carts/user/" + savedCustomer.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartItems", hasSize(3)));
    }

    //=================<< Clear Cart >>====================
    @Test
    @DisplayName("Customer can clear own cart")
    void customerCanClearOwnCart() throws Exception{
        String loginResponse = login(savedCustomer);
        String accessToken = extractAccessToken(loginResponse);

        createCartIem();
        createCartIem();
        createCartIem();

        mockMvc.perform(delete("/carts/clear/" + savedCart.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Customer cannot clear other's cart")
    void customerCannotClearOthersCart() throws Exception{
        User customer2 = createCustomer();

        String loginResponse = login(customer2);
        String accessToken = extractAccessToken(loginResponse);

        createCartIem();
        createCartIem();
        createCartIem();

        mockMvc.perform(delete("/carts/clear/" + savedCart.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin can clear any user cart")
    void customerCanClearOthersCart() throws Exception{
        User admin = createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractAccessToken(loginResponse);

        createCartIem();
        createCartIem();
        createCartIem();

        mockMvc.perform(delete("/carts/clear/" + savedCart.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk());
    }
}













