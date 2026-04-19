package com.retailstore.integration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailstore.auth.service.UserSecurityService;
import com.retailstore.category.entity.Category;
import com.retailstore.integration.BaseIntegrationTest;
import com.retailstore.order.entity.Order;
import com.retailstore.payment.enums.PaymentMethod;
import com.retailstore.product.entity.Product;
import com.retailstore.testdata.*;
import com.retailstore.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthorizationIT extends BaseIntegrationTest {

    @MockBean
    private UserSecurityService userSecurityService;

    @Autowired private UserTestData userTestData;
    @Autowired private ProductTestData productTestData;
    @Autowired private CategoryTestData categoryTestData;
    @Autowired private InventoryTestData inventoryTestData;
    @Autowired private OrderTestData orderTestData;
    @Autowired private PaymentTestData paymentTestData;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminJwt;
    private String customerJwt;

    private User savedCustomer;

    private Category savedCategory;
    private Product savedProduct;

    private Order savedOrder;

    @BeforeEach
    void setUp() throws Exception{
        savedCategory = categoryTestData.createCategory("Electronics");
        savedProduct = productTestData.createProduct(savedCategory.getId(), 100);
        inventoryTestData.createInventory(savedProduct.getId(), 22);

        User savedAdmin = userTestData.createAdmin();
        savedCustomer = userTestData.createCustomer();

        savedOrder = orderTestData.createOrder(savedCustomer.getId(), 200);
        orderTestData.createOrderItem(savedOrder.getId(), savedProduct, 2);

        //Admin login
        String adminLoginJson = """
                {
                    "email": "%s",
                    "password": "Pass1234"
                }""".formatted(savedAdmin.getEmail());

        String adminResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminLoginJson))
                .andReturn().getResponse().getContentAsString();

        adminJwt = extractToken(adminResponse);

        //Customer login
        String customerLoginJson = """
                {
                    "email": "%s",
                    "password": "Pass1234"
                }""".formatted(savedCustomer.getEmail());

        String customerResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerLoginJson))
                .andReturn().getResponse().getContentAsString();

        customerJwt = extractToken(customerResponse);
    }

    private String extractToken(String json) throws Exception {
        return objectMapper.readTree(json).get("accessToken").asText();
    }

    //===================<< Admin only Endpoints >>========================
    @Test
    void adminEndPointsShouldBeForbiddenForCustomer() throws Exception{
        String validProductJson = """
            {
                "name": "Phone",
                "description": "Latest 5G phone",
                "price": 50000,
                "categoryId": %d
            }
        """.formatted(savedCategory.getId());
        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + customerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/inventories")
                        .header("Authorization", "Bearer " + customerJwt))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/orders/%d/status".formatted(savedOrder.getId()))
                        .header("Authorization", "Bearer " + customerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"shipped\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + customerJwt))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/users/%d/role".formatted(savedCustomer.getId()))
                        .header("Authorization", "Bearer " + customerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\": \"admin\"}"))
                .andExpect(status().isForbidden());
    }

    //===================<< Admin only Endpoints >>========================
    @Test
    void adminEndpointsShouldBeAllowedForAdmin() throws Exception{
        mockMvc.perform(get("/categories")
                        .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isOk());

        mockMvc.perform(get("/inventories")
                        .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isOk());
    }

    //===================<< Customer only Endpoints >>========================
    @Test
    void customerEndpointsShouldBeAllowedForCustomer() throws Exception{
        String cartBody = """
                {
                    "userId": %d,
                    "productId": %d,
                    "quantity": %d
                }""".formatted(savedCustomer.getId(), savedProduct.getId(), 2);

        mockMvc.perform(post("/carts/add")
                        .header("Authorization", "Bearer " + customerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cartBody))
                .andExpect(status().isCreated());

        String paymentBody = """
                {
                    "orderId": %d,
                    "paymentMethod": "%s"
                }""".formatted(savedOrder.getId(), PaymentMethod.UPI);

        mockMvc.perform(post("/payments")
                        .header("Authorization", "Bearer " + customerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentBody))
                .andExpect(status().isCreated());
    }

    //===================<< Customer or admin Endpoints >>========================
    @Test
    void customerEndpointsShouldAlsoBeAllowedForAdmin() throws Exception{
        long orderId = savedOrder.getId();
        paymentTestData.createPayment(orderId, 200);
        mockMvc.perform(get("/payments/order/%d".formatted(orderId))
                        .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isOk());
    }

    //===================<< Public Endpoints >>========================
    @Test
    void publicEndpointShouldBeAccessibleWithoutJwt() throws Exception{
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/products/%d".formatted(savedProduct.getId())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk());
    }
}
