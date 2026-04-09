package com.retailstore.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailstore.auth.dto.AuthLoginRequestDTO;
import com.retailstore.integration.BaseIntegrationTest;
import com.retailstore.testdata.AddressTestData;
import com.retailstore.testdata.UserTestData;
import com.retailstore.user.dto.AddressRequestDTO;
import com.retailstore.user.entity.Address;
import com.retailstore.user.entity.User;
import com.retailstore.user.enums.AddressType;
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

public class AddressControllerIT extends BaseIntegrationTest {

    @Autowired private UserTestData userTestData;
    @Autowired private AddressTestData addressTestData;
    @Autowired private ObjectMapper objectMapper;

    private User savedCustomer;

    @BeforeEach
    void setUp(){
        savedCustomer = userTestData.createCustomer();
    }

    //=================<< Helpers >>==================
    private AddressRequestDTO buildAddressRequest(AddressType addressType){
        AddressRequestDTO request = new AddressRequestDTO();
        request.setStreet("Kargil");
        request.setCity("Hyd");
        request.setState("TS");
        request.setPincode("154793");
        request.setType(addressType);

        return request;
    }
    private String login(User user) throws Exception{
        AuthLoginRequestDTO login = new AuthLoginRequestDTO();
        login.setEmail(user.getEmail());
        login.setPassword("Pass1234");

        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private String extractToken(String json, String field) throws Exception{
        return objectMapper.readTree(json).get(field).asText();
    }

    private String authHeader(String token){
        return "Bearer " +token;
    }

    //=================<< Add address >>==================
    @Test
    @DisplayName("Customer can add an address to own profile")
    void customerCanAddAddressToOwnProfile() throws Exception{
        String loginResponse = login(savedCustomer);
        String accessToken = extractToken(loginResponse, "accessToken");

        AddressRequestDTO request = buildAddressRequest(AddressType.HOME);

        mockMvc.perform(post("/users/" + savedCustomer.getId() + "/addresses")
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pincode", is(request.getPincode())))
                .andExpect(jsonPath("$.type", is(request.getType().getValue().toLowerCase())));
    }

    @Test
    @DisplayName("Customer cannot add address to other's profile")
    void customerCannotAddAddressToOthersProfile() throws Exception{
        User customer2 = userTestData.createCustomer();

        String loginResponse = login(customer2);
        String accessToken = extractToken(loginResponse, "accessToken");

        AddressRequestDTO request = buildAddressRequest(AddressType.HOME);

        mockMvc.perform(post("/users/" + savedCustomer.getId() + "/addresses")
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin can add address to any user")
    void adminCanAddAddressToAnyUsers() throws Exception{
        User admin = userTestData.createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractToken(loginResponse, "accessToken");

        AddressRequestDTO request = buildAddressRequest(AddressType.HOME);

        mockMvc.perform(post("/users/" + savedCustomer.getId() + "/addresses")
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pincode", is(request.getPincode())))
                .andExpect(jsonPath("$.type", is(request.getType().getValue().toLowerCase())));
    }

    //=================<< Get Address By Id >>==================
    @Test
    @DisplayName("Customer can get own address")
    void customerCanGetOwnAddress() throws Exception{
        String loginResponse = login(savedCustomer);
        String accessToken = extractToken(loginResponse, "accessToken");

        Address address = addressTestData.createAddress(savedCustomer.getId(), AddressType.OFFICE);

        mockMvc.perform(get("/users/addresses/" + address.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pincode", is(address.getPincode())))
                .andExpect(jsonPath("$.type", is(address.getAddressType().name().toLowerCase())));
    }

    @Test
    @DisplayName("Customer cannot get other's address")
    void customerCannotGetOthersAddress() throws Exception{
        User customer2 = userTestData.createCustomer();

        String loginResponse = login(customer2);
        String accessToken = extractToken(loginResponse, "accessToken");

        Address address = addressTestData.createAddress(savedCustomer.getId(), AddressType.OFFICE);

        mockMvc.perform(get("/users/addresses/" + address.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin can get any user address")
    void adminCanGetAnyUserAddress() throws Exception{
        User admin = userTestData.createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractToken(loginResponse, "accessToken");

        Address address = addressTestData.createAddress(savedCustomer.getId(), AddressType.OFFICE);

        mockMvc.perform(get("/users/addresses/" + address.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pincode", is(address.getPincode())))
                .andExpect(jsonPath("$.type", is(address.getAddressType().name().toLowerCase())));
    }

    //=================<< Get All Addresses By User >>==================
    @Test
    @DisplayName("Customer can get all addresses of own profile")
    void customerCanGetAllAddressOfOwnProfile() throws Exception{
        String loginResponse = login(savedCustomer);
        String accessToken = extractToken(loginResponse, "accessToken");

        addressTestData.createAddress(savedCustomer.getId(), AddressType.OFFICE);
        addressTestData.createAddress(savedCustomer.getId(), AddressType.HOME);

        mockMvc.perform(get("/users/" + savedCustomer.getId() + "/addresses")
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Customer cannot get addresses of other's profile")
    void customerCannotGetAddressOfOthersProfile() throws Exception{
        User customer2 = userTestData.createCustomer();

        String loginResponse = login(customer2);
        String accessToken = extractToken(loginResponse, "accessToken");

        addressTestData.createAddress(savedCustomer.getId(), AddressType.OFFICE);
        addressTestData.createAddress(savedCustomer.getId(), AddressType.HOME);

        mockMvc.perform(get("/users/" + savedCustomer.getId() + "/addresses")
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin can get addresses of any user")
    void adminCanGetAddressOfAnyUser() throws Exception{
        User admin = userTestData.createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractToken(loginResponse, "accessToken");

        addressTestData.createAddress(savedCustomer.getId(), AddressType.OFFICE);
        addressTestData.createAddress(savedCustomer.getId(), AddressType.HOME);

        mockMvc.perform(get("/users/" + savedCustomer.getId() + "/addresses")
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    //=================<< Update Address >>==================
    @Test
    @DisplayName("Customer can update own address")
    void customerCanUpdateOwnAddress() throws Exception{
        String loginResponse = login(savedCustomer);
        String accessToken = extractToken(loginResponse, "accessToken");

        Address address = addressTestData.createAddress(savedCustomer.getId(), AddressType.OFFICE);
        AddressRequestDTO request = buildAddressRequest(AddressType.SHIPPING);

        mockMvc.perform(put("/users/addresses/" + address.getId())
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pincode", is(address.getPincode())))
                .andExpect(jsonPath("$.type", is(address.getAddressType().name().toLowerCase())));
    }

    @Test
    @DisplayName("Customer cannot update other's address")
    void customerCannotUpdateOthersAddress() throws Exception{
        User customer2 = userTestData.createCustomer();

        String loginResponse = login(customer2);
        String accessToken = extractToken(loginResponse, "accessToken");

        Address address = addressTestData.createAddress(savedCustomer.getId(), AddressType.OFFICE);
        AddressRequestDTO request = buildAddressRequest(AddressType.BILLING);

        mockMvc.perform(put("/users/addresses/" + address.getId())
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin can update any user address")
    void adminCanUpdateAnyUserAddress() throws Exception{
        User admin = userTestData.createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractToken(loginResponse, "accessToken");

        Address address = addressTestData.createAddress(savedCustomer.getId(), AddressType.OFFICE);
        AddressRequestDTO request = buildAddressRequest(AddressType.BILLING);

        mockMvc.perform(put("/users/addresses/" + address.getId())
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pincode", is(address.getPincode())))
                .andExpect(jsonPath("$.type", is(address.getAddressType().name().toLowerCase())));
    }

    //=================<< Delete Address >>==================
    @Test
    @DisplayName("Customer can delete own address")
    void customerCanDeleteOwnAddress() throws Exception{
        String loginResponse = login(savedCustomer);
        String accessToken = extractToken(loginResponse, "accessToken");

        Address address = addressTestData.createAddress(savedCustomer.getId(), AddressType.OFFICE);

        mockMvc.perform(delete("/users/addresses/" + address.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Customer delete get other's address")
    void customerCannotDeleteOthersAddress() throws Exception{
        User customer2 = userTestData.createCustomer();

        String loginResponse = login(customer2);
        String accessToken = extractToken(loginResponse, "accessToken");

        Address address = addressTestData.createAddress(savedCustomer.getId(), AddressType.OFFICE);

        mockMvc.perform(delete("/users/addresses/" + address.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin can delete any user address")
    void adminCanDeleteAnyUserAddress() throws Exception{
        User admin = userTestData.createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractToken(loginResponse, "accessToken");

        Address address = addressTestData.createAddress(savedCustomer.getId(), AddressType.OFFICE);

        mockMvc.perform(delete("/users/addresses/" + address.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk());
    }
}
