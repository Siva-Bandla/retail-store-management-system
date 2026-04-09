package com.retailstore.system;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailstore.auth.dto.AuthLoginRequestDTO;
import com.retailstore.user.dto.UserRegisterRequestDTO;
import com.retailstore.user.enums.UserRole;
import org.junit.jupiter.api.*;
import org.springframework.http.*;

import java.util.Objects;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EndToEndWorkflowTest extends SystemTestBase {

    private final ObjectMapper mapper = new ObjectMapper();

    private Long customerId;
    private Long productId;
    private Long orderId;

    private String getField(String json){
        try{
            JsonNode node = mapper.readTree(json);
            return node.get("accessToken").asText();
        }catch (Exception e){
            return null;
        }
    }

    private Long getLong(String json, String field){
        try {
            JsonNode node = mapper.readTree(json);
            return node.get(field).asLong();
        }catch (Exception e){
            return null;
        }
    }

    private UserRegisterRequestDTO buildUserRegister(String email, UserRole userRole){
        UserRegisterRequestDTO register = new UserRegisterRequestDTO();
        register.setEmail(email);
        register.setPassword("Password1234");
        register.setName("System Tester");
        register.setRole(userRole);

        return register;
    }

    private String loginUser(String email){
        AuthLoginRequestDTO login = new AuthLoginRequestDTO();
        login.setEmail(email);
        login.setPassword("Password1234");

        ResponseEntity<String> response =
                testRestTemplate.postForEntity(baseUrl + "/api/auth/login", login, String.class);

        Assertions.assertEquals(200, response.getStatusCode().value());

        return getField(response.getBody());
    }

    //==============<< Register User >>===============
    @Test
    @Order(1)
    void registerUsers(){
        UserRegisterRequestDTO admin = buildUserRegister("admin@test.com", UserRole.ROLE_ADMIN);
        UserRegisterRequestDTO customer = buildUserRegister("customer@test.com", UserRole.ROLE_CUSTOMER);

        ResponseEntity<String> adminResponse =
                testRestTemplate.postForEntity(baseUrl + "/users/register", admin, String.class);
        ResponseEntity<String> customerResponse =
                testRestTemplate.postForEntity(baseUrl + "/users/register", customer, String.class);

        Assertions.assertEquals(201, adminResponse.getStatusCode().value());
        Assertions.assertEquals(201, customerResponse.getStatusCode().value());

        customerId = getLong(customerResponse.getBody(), "id");
    }

    //==============<< Login + capture token >>===============
    @Test
    @Order(2)
    void loginUser(){
        adminToken = loginUser("admin@test.com");
        customerToken = loginUser("customer@test.com");

        Assertions.assertNotNull(adminToken, "Admin token missing from login response!");
        Assertions.assertNotNull(customerToken, "Customer token missing from login response!");
    }

    //==============<< Create Product >>===============
    @Test
    @Order(3)
    void createProduct(){
        HttpHeaders headers = authHeaders(adminToken);

        String json = """
                {
                    "name": "Laptop",
                    "description": "Lenovo Think Pad",
                    "price": 59999,
                    "quantity": 16,
                    "categoryId": 1
                }
                """;

        HttpEntity<String> request = new HttpEntity<>(json, headers);

        ResponseEntity<String> response =
                testRestTemplate.postForEntity(baseUrl + "/products", request, String.class);

        Assertions.assertEquals(201, response.getStatusCode().value());

        productId = getLong(response.getBody(), "id");
        Assertions.assertNotNull(productId);
    }

    //==============<< Add Item to Cart >>===============
    @Test
    @Order(4)
    void addItemToCart(){
        HttpHeaders headers = authHeaders(customerToken);

        String json = """
            {"userId": %d, "productId": %d, "quantity": 3}
            """.formatted(customerId, productId);

        HttpEntity<String> request = new HttpEntity<>(json, headers);

        ResponseEntity<String> response =
                testRestTemplate.exchange(baseUrl + "/carts/add", HttpMethod.POST, request, String.class);

        Assertions.assertEquals(201, response.getStatusCode().value());
    }

    //==============<< Place Order >>===============
    @Test
    @Order(5)
    void placeOrder(){
        HttpHeaders headers = authHeaders(customerToken);

        String json = """
            {
                "userId": %d,
                "items": [
                    {"productId": %d, "quantity": 3}
                ]
            }
            """.formatted(customerId, productId);

        HttpEntity<String> request = new HttpEntity<>(json, headers);

        ResponseEntity<String> response =
                testRestTemplate.exchange(baseUrl + "/orders", HttpMethod.POST, request, String.class);

        Assertions.assertEquals(201, response.getStatusCode().value());

        orderId = getLong(response.getBody(), "orderId");
        Assertions.assertNotNull(orderId);
    }

    //==============<< Make Payment >>===============
    @Test
    @Order(6)
    void makePayment(){
        HttpHeaders headers = authHeaders(customerToken);

        String json = """
                {"orderId": %d, "paymentMethod": "upi"}
                """.formatted(orderId);

        HttpEntity<String> request = new HttpEntity<>(json, headers);

        ResponseEntity<String> response =
                testRestTemplate.postForEntity(baseUrl + "/payments", request, String.class);

        Assertions.assertEquals(201, response.getStatusCode().value());
    }

    //==============<< Validate Inventory >>===============
    @Test
    @Order(7)
    void verifyInventoryReduced(){
        HttpEntity<Void> request = new HttpEntity<>(authHeaders(adminToken));

        ResponseEntity<String> response =
                testRestTemplate.exchange(baseUrl + "/inventories/" + productId, HttpMethod.GET, request, String.class);

        Assertions.assertEquals(200, response.getStatusCode().value());

        int remainingStock = Objects.requireNonNull(getLong(response.getBody(), "stock")).intValue();
        Assertions.assertEquals(13, remainingStock);
    }
}