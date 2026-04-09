package com.retailstore.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailstore.auth.dto.AuthLoginRequestDTO;
import com.retailstore.cart.entity.Cart;
import com.retailstore.category.entity.Category;
import com.retailstore.integration.BaseIntegrationTest;
import com.retailstore.order.dto.CreateOrderRequestDTO;
import com.retailstore.order.dto.OrderItemRequestDTO;
import com.retailstore.order.dto.UpdateOrderStatusRequestDTO;
import com.retailstore.order.entity.Order;
import com.retailstore.order.enums.OrderStatus;
import com.retailstore.product.entity.Product;
import com.retailstore.testdata.*;
import com.retailstore.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderControllerIT extends BaseIntegrationTest {

    @Autowired private UserTestData userTestData;
    @Autowired private CategoryTestData categoryTestData;
    @Autowired private ProductTestData productTestData;
    @Autowired private InventoryTestData inventoryTestData;
    @Autowired private CartTestData cartTestData;
    @Autowired private OrderTestData orderTestData;
    @Autowired private ObjectMapper objectMapper;

    private User savedCustomer;
    private Category savedCategory;
    private Product savedProduct;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        savedCustomer = createCustomer();
        savedCategory = categoryTestData.createCategory("Electronics");
        savedProduct = createProduct();
        savedOrder = orderTestData.createOrder(savedCustomer.getId(), 300);
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

    private CreateOrderRequestDTO buildOrderRequest(List<OrderItemRequestDTO> orderItems){
        CreateOrderRequestDTO request = new CreateOrderRequestDTO();
        request.setUserId(savedCustomer.getId());
        request.setItems(orderItems);

        return request;
    }

    private OrderItemRequestDTO buildOrderItemRequest(Long productId, int quantity){
        OrderItemRequestDTO itemRequest = new OrderItemRequestDTO();
        itemRequest.setProductId(productId);
        itemRequest.setQuantity(quantity);

        return itemRequest;
    }

    private void createOrderItemIem(){
        orderTestData.createOrderItem(savedOrder.getId(), savedProduct, 2);
    }

    private void createCart(Long userId){
        Cart savedCart = cartTestData.createCart(userId);
        cartTestData.createCartItem(savedCart.getId(), savedProduct.getId(), 2);
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

    //=================<< Create Order >>====================
    @Test
    @DisplayName("Customer can create order to own profile")
    void customerCanCreateOrderToOwnProfile() throws Exception{
        String loginResponse = login(savedCustomer);
        String accessToken = extractAccessToken(loginResponse);

        OrderItemRequestDTO item1 = buildOrderItemRequest(savedProduct.getId(), 3);
        Product product2 = createProduct();
        OrderItemRequestDTO item2 = buildOrderItemRequest(product2.getId(), 5);
        CreateOrderRequestDTO orderRequest = buildOrderRequest(List.of(item1, item2));

        mockMvc.perform(post("/orders")
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", is(savedCustomer.getId().intValue())))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.status", is(OrderStatus.CREATED.name().toLowerCase())));
    }

    @Test
    @DisplayName("Customer cannot create order for other's profile")
    void customerCannotCreateOrderToOthersProfile() throws Exception{
        User customer2 = createCustomer();

        String loginResponse = login(customer2);
        String accessToken = extractAccessToken(loginResponse);

        OrderItemRequestDTO item1 = buildOrderItemRequest(savedProduct.getId(), 3);
        Product product2 = createProduct();
        OrderItemRequestDTO item2 = buildOrderItemRequest(product2.getId(), 5);
        CreateOrderRequestDTO orderRequest = buildOrderRequest(List.of(item1, item2));

        mockMvc.perform(post("/orders")
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin cannot create order for other's profile")
    void adminCannotCreateOrderToOthersProfile() throws Exception{
        User admin = createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractAccessToken(loginResponse);

        OrderItemRequestDTO item1 = buildOrderItemRequest(savedProduct.getId(), 3);
        Product product2 = createProduct();
        OrderItemRequestDTO item2 = buildOrderItemRequest(product2.getId(), 5);
        CreateOrderRequestDTO orderRequest = buildOrderRequest(List.of(item1, item2));

        mockMvc.perform(post("/orders")
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isForbidden());
    }

    //=================<< Get Order By Id >>====================
    @Test
    @DisplayName("Customer can get order from own profile")
    void customerCanGetOrderFromOwnProfile() throws Exception{
        String loginResponse = login(savedCustomer);
        String accessToken = extractAccessToken(loginResponse);

        createOrderItemIem();
        createOrderItemIem();
        createOrderItemIem();

        mockMvc.perform(get("/orders/" + savedOrder.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Test
    @DisplayName("Customer cannot get order from other's profile")
    void customerCannotGetOrderFromOthersProfile() throws Exception{
        User customer2 = createCustomer();

        String loginResponse = login(customer2);
        String accessToken = extractAccessToken(loginResponse);

        createOrderItemIem();
        createOrderItemIem();
        createOrderItemIem();

        mockMvc.perform(get("/orders/" + savedOrder.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin cannot get order from other's profile")
    void adminCannotGetOrderFromOthersProfile() throws Exception{
        User admin = createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractAccessToken(loginResponse);

        createOrderItemIem();
        createOrderItemIem();
        createOrderItemIem();

        mockMvc.perform(get("/orders/" + savedOrder.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isForbidden());
    }

    //=================<< Get All Orders (Admin) >>====================
    @Test
    @DisplayName("Admin can get all orders")
    void adminCanGetAllOrders() throws Exception{
        User admin = createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractAccessToken(loginResponse);

        createOrderItemIem();
        createOrderItemIem();
        createOrderItemIem();

        mockMvc.perform(get("/orders")
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].items", hasSize(3)));
    }

    @Test
    @DisplayName("Customer cannot get all orders (others included)")
    void customerCannotGetAllOrders() throws Exception{
        String loginResponse = login(savedCustomer);
        String accessToken = extractAccessToken(loginResponse);

        createOrderItemIem();
        createOrderItemIem();
        createOrderItemIem();

        mockMvc.perform(get("/orders")
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isForbidden());
    }

    //=================<< Get Orders By User >>====================
    @Test
    @DisplayName("Customer can get orders from own profile")
    void customerCanGetOrdersFromOwnProfile() throws Exception{
        String loginResponse = login(savedCustomer);
        String accessToken = extractAccessToken(loginResponse);

        createOrderItemIem();
        createOrderItemIem();
        createOrderItemIem();

        mockMvc.perform(get("/orders/user/" + savedCustomer.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].items", hasSize(3)));
    }

    @Test
    @DisplayName("Customer cannot get orders from other's profile")
    void customerCannotGetOrdersFromOthersProfile() throws Exception{
        User customer2 = createCustomer();

        String loginResponse = login(customer2);
        String accessToken = extractAccessToken(loginResponse);

        createOrderItemIem();
        createOrderItemIem();
        createOrderItemIem();

        mockMvc.perform(get("/orders/user/" + savedCustomer.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin cannot get orders from other's profile")
    void adminCannotGetOrdersFromOthersProfile() throws Exception{
        User admin = createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractAccessToken(loginResponse);

        createOrderItemIem();
        createOrderItemIem();
        createOrderItemIem();

        mockMvc.perform(get("/orders/user/" + savedCustomer.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isForbidden());
    }

    //=================<< Update Order By Status (Admin) >>====================
    @Test
    @DisplayName("Admin can update any order status")
    void adminCanUpdateAnyOrderStatus() throws Exception{
        User admin = createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractAccessToken(loginResponse);

        createOrderItemIem();
        createOrderItemIem();
        createOrderItemIem();

        UpdateOrderStatusRequestDTO statusRequest = new UpdateOrderStatusRequestDTO();
        statusRequest.setStatus(OrderStatus.SHIPPED);

        mockMvc.perform(patch("/orders/" + savedOrder.getId() + "/status")
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(savedOrder.getStatus().name().toLowerCase())));
    }

    @Test
    @DisplayName("Customer cannot update order status")
    void customerCannotUpdateOrderStatus() throws Exception{
        String loginResponse = login(savedCustomer);
        String accessToken = extractAccessToken(loginResponse);

        createOrderItemIem();
        createOrderItemIem();
        createOrderItemIem();

        UpdateOrderStatusRequestDTO statusRequest = new UpdateOrderStatusRequestDTO();
        statusRequest.setStatus(OrderStatus.SHIPPED);

        mockMvc.perform(patch("/orders/" + savedOrder.getId() + "/status")
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isForbidden());
    }

    //=================<< Cancel Order >>====================
    @Test
    @DisplayName("Customer can cancel order from own profile")
    void customerCanCancelOrderFromOwnProfile() throws Exception{
        String loginResponse = login(savedCustomer);
        String accessToken = extractAccessToken(loginResponse);

        createOrderItemIem();
        createOrderItemIem();
        createOrderItemIem();

        mockMvc.perform(delete("/orders/" + savedOrder.getId() + "/cancel")
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Customer cannot cancel order from other's profile")
    void customerCannotCancelOrderFromOthersProfile() throws Exception{
        User customer2 = createCustomer();

        String loginResponse = login(customer2);
        String accessToken = extractAccessToken(loginResponse);

        createOrderItemIem();
        createOrderItemIem();
        createOrderItemIem();

        mockMvc.perform(delete("/orders/" + savedOrder.getId() + "/cancel")
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin can cancel order from other's profile")
    void adminCanCancelOrderFromOthersProfile() throws Exception{
        User admin = createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractAccessToken(loginResponse);

        createOrderItemIem();
        createOrderItemIem();
        createOrderItemIem();

        mockMvc.perform(delete("/orders/" + savedOrder.getId() + "/cancel")
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk());
    }

    //=================<< Checkout >>====================
    @Test
    @DisplayName("Customer can create order from own cart")
    void customerCanCreateOrderFromOwnCart() throws Exception{
        String loginResponse = login(savedCustomer);
        String accessToken = extractAccessToken(loginResponse);

        createCart(savedCustomer.getId());

        mockMvc.perform(post("/orders/checkout/" + savedCustomer.getId())
                .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(savedCustomer.getId().intValue())))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.status", is(OrderStatus.CREATED.name().toLowerCase())));
    }

    @Test
    @DisplayName("Customer cannot create order from other's cart")
    void customerCannotCreateOrderFromOthersCart() throws Exception{
        User customer2 = createCustomer();

        String loginResponse = login(customer2);
        String accessToken = extractAccessToken(loginResponse);

        createCart(savedCustomer.getId());

        mockMvc.perform(post("/orders/checkout/" + savedCustomer.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin cannot create order from other's cart")
    void adminCannotCreateOrderFromOthersCart() throws Exception{
        User admin = createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractAccessToken(loginResponse);

        createCart(savedCustomer.getId());

        mockMvc.perform(post("/orders/checkout/" + savedCustomer.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isForbidden());
    }
}
