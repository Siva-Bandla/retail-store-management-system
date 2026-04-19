package com.retailstore.integration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailstore.category.entity.Category;
import com.retailstore.integration.BaseIntegrationTest;
import com.retailstore.order.entity.Order;
import com.retailstore.payment.enums.PaymentMethod;
import com.retailstore.product.entity.Product;
import com.retailstore.testdata.*;
import com.retailstore.user.entity.User;
import com.retailstore.user.enums.AddressType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SecurityConfigIT extends BaseIntegrationTest {

    @Autowired private UserTestData userTestData;
    @Autowired private CategoryTestData categoryTestData;
    @Autowired private ProductTestData productTestData;
    @Autowired private InventoryTestData inventoryTestData;
    @Autowired private OrderTestData orderTestData;
    @Autowired private PaymentTestData paymentTestData;

    @Autowired private ObjectMapper objectMapper;

    private Category savedCategory;
    private Product savedProduct;
    private User savedCustomer;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        savedCategory = categoryTestData.createCategory("Electronics");
        savedProduct = productTestData.createProduct(savedCategory.getId(), 100);
        inventoryTestData.createInventory(savedProduct.getId(), 22);

        savedCustomer = userTestData.createCustomer();

        savedOrder = orderTestData.createOrder(savedCustomer.getId(), 200);
        orderTestData.createOrderItem(savedOrder.getId(), savedProduct, 2);
    }

    //============<< Public Endpoints >>================
    @Test
    void publicEndpointsShouldBeAccessibleWithoutAuthentication() throws Exception{
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/products/%d".formatted(savedProduct.getId())))
                .andExpect(status().isOk());

        String customerLoginJson = """
                {
                    "email": "%s",
                }""".formatted(savedCustomer.getEmail());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerLoginJson))
                .andExpect(status().isBadRequest());

        String refreshBody = """
                {"refreshToken": "invalid-token"}""";
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isNotFound());
    }

    //============<< Protected Endpoints >>================
    @Test
    void protectedUserEndpointsShouldRequireJwt() throws Exception{
        mockMvc.perform(get("/users/%d".formatted(savedCustomer.getId())))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/users/%d".formatted(savedCustomer.getId())))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/users/%d".formatted(savedCustomer.getId())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedCartEndpointsShouldRequireJwt() throws Exception{
        String cartBody = """
                {
                    "userId": %d,
                    "productId": %d,
                    "quantity": %d
                }""".formatted(savedCustomer.getId(), savedProduct.getId(), 2);

        mockMvc.perform(post("/carts/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cartBody))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/carts/user/%d".formatted(savedCustomer.getId())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedOrderEndpointsShouldRequireJwt() throws Exception{
        String orderBody = """
                {
                    "userId": %d,
                    "items": [ { "productId": %d, "quantity": %d} ]
                }""".formatted(savedCustomer.getId(), savedProduct.getId(), 2);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/orders/%d".formatted(savedOrder.getId())))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedPaymentEndpointsShouldRequireJwt() throws Exception{
        String paymentBody = """
                {
                    "orderId": %d,
                    "paymentMethod": "%s"
                }""".formatted(savedOrder.getId(), PaymentMethod.WALLET);

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentBody))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/payments/order/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedAddressEndpointsShouldRequireJwt() throws Exception{
        String addressBody = """
                {
                    "street": "Kargil",
                    "city": "Hyd",
                    "state": "TS",
                    "pincode":"548792"
                    "type": "%s"
                }""".formatted(AddressType.HOME);

        mockMvc.perform(post("/users/%d/addresses".formatted(savedCustomer.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addressBody))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/users/%d/addresses".formatted(savedCustomer.getId())))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/users/addresses/%d".formatted(savedCustomer.getId())))
                .andExpect(status().isUnauthorized());
    }

    //============<< Admin only Endpoints >>================
    @Test
    void adminCategoryEndpointsShouldRequireJwt() throws Exception{
        String categoryBody = """
                {
                    "name": "Clothes",
                    "description": "Men and Women wear"
                }""";

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryBody))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/categories/%d".formatted(savedCategory.getId())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminInventoryEndpointsShouldRequireJwt() throws Exception{
        String inventoryBody = """
                {
                    "productId": %d,
                    "stock": %d
                }""".formatted(savedProduct.getId(), 2);

        mockMvc.perform(post("/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inventoryBody))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/inventories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminOrderStatusEndpointsShouldRequireJwt() throws Exception{
        mockMvc.perform(patch("/orders/%d/status".formatted(savedOrder.getId()))
                        .param("status", "SHIPPED"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminUserManagementEndpointsShouldRequireJwt() throws Exception{
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/users/%d/role".formatted(savedCustomer.getId()))
                        .param("role", "ADMIN"))
                .andExpect(status().isUnauthorized());
    }

    //============<< Invalid methods / unsupported >>================
    @Test
    void unsupportedMethodShouldReturnMethodNotAllowed() throws Exception {
        String customerLoginJson = """
                {
                    "email": "%s",
                    "password": "Pass1234"
                }""".formatted(savedCustomer.getEmail());

        String customerResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerLoginJson))
                .andReturn().getResponse().getContentAsString();

        String jwt = objectMapper.readTree(customerResponse).get("accessToken").asText();

        mockMvc.perform(patch("/products")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isMethodNotAllowed());
    }
}
