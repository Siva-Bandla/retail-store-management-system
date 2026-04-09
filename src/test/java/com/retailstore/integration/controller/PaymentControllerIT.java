package com.retailstore.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailstore.auth.dto.AuthLoginRequestDTO;
import com.retailstore.category.entity.Category;
import com.retailstore.integration.BaseIntegrationTest;
import com.retailstore.order.entity.Order;
import com.retailstore.payment.dto.PaymentRequestDTO;
import com.retailstore.payment.entity.Payment;
import com.retailstore.payment.enums.PaymentMethod;
import com.retailstore.payment.enums.PaymentStatus;
import com.retailstore.payment.repository.PaymentRepository;
import com.retailstore.product.entity.Product;
import com.retailstore.testdata.*;
import com.retailstore.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PaymentControllerIT extends BaseIntegrationTest {

    @Autowired private UserTestData userTestData;
    @Autowired private CategoryTestData categoryTestData;
    @Autowired private ProductTestData productTestData;
    @Autowired private InventoryTestData inventoryTestData;
    @Autowired private OrderTestData orderTestData;
    @Autowired private PaymentTestData paymentTestData;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private PaymentRepository paymentRepository;

    private User savedCustomer;
    private Category savedCategory;
    private Order savedOrder;
    private Payment savedPayment;

    @BeforeEach
    void setUp() {
        savedCustomer = createCustomer();
        savedCategory = categoryTestData.createCategory("Electronics");
        Product savedProduct = createProduct();
        savedOrder = orderTestData.createOrder(savedCustomer.getId(), 300);
        savedPayment = createPayment();
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

    private PaymentRequestDTO buildPaymentRequest(Long orderId){
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setOrderId(orderId);
        request.setPaymentMethod(PaymentMethod.NET_BANKING);

        return request;
    }

    private Payment createPayment(){
        return paymentTestData.createPayment(savedOrder.getId(), 300);
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

    //=================<< Process Payment >>====================
    @Test
    @DisplayName("Customer can process payment to own order")
    void customerCanProcessPaymentOwnOrder() throws Exception{
        String loginResponse = login(savedCustomer);
        String accessToken = extractAccessToken(loginResponse);

        paymentRepository.delete(savedPayment);

        PaymentRequestDTO request = buildPaymentRequest(savedOrder.getId());

        mockMvc.perform(post("/payments")
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId", is(request.getOrderId().intValue())))
                .andExpect(jsonPath("$.paymentStatus", is(PaymentStatus.SUCCESS.name().toLowerCase())));
    }

    @Test
    @DisplayName("Customer cannot process payment to other's order")
    void customerCannotProcessPaymentOthersOrder() throws Exception{
        User customer2 = createCustomer();

        String loginResponse = login(customer2);
        String accessToken = extractAccessToken(loginResponse);

        PaymentRequestDTO request = buildPaymentRequest(savedOrder.getId());

        mockMvc.perform(post("/payments")
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin cannot process payment to other's order")
    void adminCannotProcessPaymentOthersOrder() throws Exception{
        User admin = createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractAccessToken(loginResponse);

        PaymentRequestDTO request = buildPaymentRequest(savedOrder.getId());

        mockMvc.perform(post("/payments")
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    //=================<< Get Payment By Order Id >>====================
    @Test
    @DisplayName("Customer can get own payment by order id")
    void customerCanGetOwnPaymentByOrderId() throws Exception{
        String loginResponse = login(savedCustomer);
        String accessToken = extractAccessToken(loginResponse);

        mockMvc.perform(get("/payments/order/" + savedOrder.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus", is(savedPayment.getPaymentStatus().name().toLowerCase())));
    }

    @Test
    @DisplayName("Customer cannot get other's payment by order id")
    void customerCannotGetOthersPaymentByOrderId() throws Exception{
        User customer2 = createCustomer();

        String loginResponse = login(customer2);
        String accessToken = extractAccessToken(loginResponse);

        mockMvc.perform(get("/payments/order/" + savedOrder.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin can get other's payment by order id")
    void adminCanGetOthersPaymentByOrderId() throws Exception{
        User admin = createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractAccessToken(loginResponse);

        mockMvc.perform(get("/payments/order/" + savedOrder.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus", is(savedPayment.getPaymentStatus().name().toLowerCase())));
    }

    //=================<< Get Payment By Payment Id >>====================
    @Test
    @DisplayName("Customer can get own payment by id")
    void customerCanGetOwnPaymentById() throws Exception{
        String loginResponse = login(savedCustomer);
        String accessToken = extractAccessToken(loginResponse);

        mockMvc.perform(get("/payments/" + savedPayment.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus", is(savedPayment.getPaymentStatus().name().toLowerCase())));
    }

    @Test
    @DisplayName("Customer cannot get other's payment by id")
    void customerCannotGetOthersPaymentById() throws Exception{
        User customer2 = createCustomer();

        String loginResponse = login(customer2);
        String accessToken = extractAccessToken(loginResponse);

        mockMvc.perform(get("/payments/" + savedPayment.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin can get other's payment by id")
    void adminCanGetOthersPaymentById() throws Exception{
        User admin = createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractAccessToken(loginResponse);

        mockMvc.perform(get("/payments/" + savedPayment.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus", is(savedPayment.getPaymentStatus().name().toLowerCase())));
    }

    //=================<< Refund Payment (Admin) >>====================
    @Test
    @DisplayName("Admin can refund payment")
    void adminCanRefundPayment() throws Exception{
        User admin = createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractAccessToken(loginResponse);

        mockMvc.perform(post("/payments/" + savedOrder.getId() + "/refund")
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus", is(savedPayment.getPaymentStatus().name().toLowerCase())));
    }
    @Test
    @DisplayName("Customer cannot refund payment")
    void customerCannotRefundPayment() throws Exception{
        String loginResponse = login(savedCustomer);
        String accessToken = extractAccessToken(loginResponse);

        mockMvc.perform(post("/payments/" + savedOrder.getId() + "/refund")
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isForbidden());
    }
}
