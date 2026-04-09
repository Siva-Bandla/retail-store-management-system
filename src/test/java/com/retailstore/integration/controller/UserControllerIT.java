package com.retailstore.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailstore.auth.dto.AuthLoginRequestDTO;
import com.retailstore.integration.BaseIntegrationTest;
import com.retailstore.testdata.UserTestData;
import com.retailstore.user.dto.UpdateUserRequestDTO;
import com.retailstore.user.dto.UpdateUserRoleRequestDTO;
import com.retailstore.user.dto.UserRegisterRequestDTO;
import com.retailstore.user.entity.User;
import com.retailstore.user.enums.UserRole;
import com.retailstore.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerIT extends BaseIntegrationTest {

    @Autowired private UserTestData userTestData;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void setUp(){
        userRepository.deleteAll();
    }

    //==============<< Helpers >>===============
    private User createCustomer() { return userTestData.createCustomer(); }
    private User createAdmin() { return userTestData.createAdmin(); }

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

    private String extractToken(String loginResponse, String field) throws Exception {
        return objectMapper.readTree(loginResponse).get(field).asText();
    }

    private String authHeader(String token){
        return "Bearer " + token;
    }

    //==============<< Helpers >>===============
    @Test
    @DisplayName("should register user successfully")
    void shouldRegisterUser() throws Exception{
        UserRegisterRequestDTO register = new UserRegisterRequestDTO();
        register.setEmail("newuser@example.com");
        register.setPassword("pass7920");
        register.setName("New User");
        register.setRole(UserRole.ROLE_CUSTOMER);

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("newuser@example.com")));
    }

    @Test
    @DisplayName("should fail registration when email already exists")
    void shouldFailDuplicateEmailRegistration() throws Exception{
        User savedCustomer = createCustomer();

        UserRegisterRequestDTO register = new UserRegisterRequestDTO();
        register.setEmail(savedCustomer.getEmail());
        register.setPassword("pass7920");
        register.setName("New User");
        register.setRole(UserRole.ROLE_CUSTOMER);

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    //==============<< Get User By Id >>===============
    @Test
    @DisplayName("Customer can fetch own profile")
    void shouldGetOwnProfile() throws Exception{
        User savedCustomer = createCustomer();

        String loginResponse = login(savedCustomer);
        String accessToken = extractToken(loginResponse, "accessToken");

        mockMvc.perform(get("/users/" + savedCustomer.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedCustomer.getId().intValue())));
    }
    @Test
    @DisplayName("customer cannot fetch other user's profile")
    void shouldRejectFetchingOthersProfile() throws Exception{
        User customer1 = createCustomer();
        User customer2 = createCustomer();

        String loginResponse = login(customer1);
        String accessToken = extractToken(loginResponse, "accessToken");

        mockMvc.perform(get("/users/" + customer2.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("admin can fetch any user")
    void adminCanFetchAnyUser() throws Exception{
        User admin = createAdmin();
        User customer = createCustomer();

        String adminLoginResponse = login(admin);
        String accessToken = extractToken(adminLoginResponse, "accessToken");

        mockMvc.perform(get("/users/"+customer.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(customer.getEmail())));
    }

    //==============<< Update User >>===============
    @Test
    @DisplayName("customer can update own profile")
    void customerUpdatesOwnProfile() throws Exception {
        User customer = createCustomer();

        String loginResponse = login(customer);
        String accessToken = extractToken(loginResponse, "accessToken");

        UpdateUserRequestDTO request = new UpdateUserRequestDTO();
        request.setEmail(customer.getEmail());
        request.setPassword("Password48416");
        request.setPhone("7896541235");
        request.setName("Updated User");

        mockMvc.perform(put("/users/" + customer.getId())
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(customer.getName())));
    }

    @Test
    @DisplayName("customer cannot update other's profile")
    void customerCannotUpdateOthersProfile() throws Exception{
        User customer1 = createCustomer();
        User customer2 = createCustomer();

        String loginResponse = login(customer1);
        String accessToken = extractToken(loginResponse, "accessToken");

        UpdateUserRequestDTO request = new UpdateUserRequestDTO();
        request.setEmail(customer2.getEmail());
        request.setPassword("Password48416");
        request.setPhone("7896541235");
        request.setName("User Hack");

        mockMvc.perform(put("/users/" + customer2.getId())
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("admin can update any user")
    void adminCanUpdateAnyUser() throws Exception{
        User admin = createAdmin();
        User customer = createCustomer();

        String adminLoginResponse = login(admin);
        String accessToken = extractToken(adminLoginResponse, "accessToken");

        UpdateUserRequestDTO request = new UpdateUserRequestDTO();
        request.setEmail(customer.getEmail());
        request.setPassword("Password48416");
        request.setPhone("15487952326");
        request.setName("Admin Updated");

        mockMvc.perform(get("/users/"+customer.getId())
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(customer.getEmail())))
                .andExpect(jsonPath("$.name", is(customer.getName())));
    }

    //==============<< Delete User >>===============
    @Test
    @DisplayName("customer can delete own profile")
    void customerDeleteOwnProfile() throws Exception {
        User customer = createCustomer();

        String loginResponse = login(customer);
        String accessToken = extractToken(loginResponse, "accessToken");

        mockMvc.perform(delete("/users/" + customer.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("customer cannot delete other's profile")
    void customerCannotDeleteOthersProfile() throws Exception{
        User customer1 = createCustomer();
        User customer2 = createCustomer();

        String loginResponse = login(customer1);
        String accessToken = extractToken(loginResponse, "accessToken");

        mockMvc.perform(delete("/users/" + customer2.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("admin can delete any user")
    void adminCanDeleteAnyUser() throws Exception{
        User admin = createAdmin();
        User customer = createCustomer();

        String adminLoginResponse = login(admin);
        String accessToken = extractToken(adminLoginResponse, "accessToken");

        mockMvc.perform(delete("/users/"+customer.getId())
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk());
    }

    //==============<< Get All Users (Admin) >>===============
    @Test
    @DisplayName("admin can fetch all users")
    void adminCanFetchAllUsers() throws Exception{
        User admin = createAdmin();
        createCustomer();
        createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractToken(loginResponse, "accessToken");

        mockMvc.perform(get("/users")
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("customer cannot fetch all users")
    void customerCannotFetchAllUsers() throws Exception{
        User customer = createCustomer();
        createCustomer();

        String loginResponse = login(customer);
        String accessToken = extractToken(loginResponse, "accessToken");

        mockMvc.perform(get("/users")
                        .header("Authorization", authHeader(accessToken)))
                .andExpect(status().isForbidden());
    }

    //==============<< Update User Role (Admin) >>===============
    @Test
    @DisplayName("admin can update any user's role")
    void adminCanUpdateAnyUserRole() throws Exception{
        User customer = createCustomer();
        User admin = createAdmin();

        String loginResponse = login(admin);
        String accessToken = extractToken(loginResponse, "accessToken");

        UpdateUserRoleRequestDTO request = new UpdateUserRoleRequestDTO();
        request.setRole(UserRole.ROLE_ADMIN);

        mockMvc.perform(patch("/users/" + customer.getId() + "/role")
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(customer.getEmail())))
                .andExpect(jsonPath("$.role",
                        is(customer.getRole().name().replace("ROLE_", "").toLowerCase())));
    }

    @Test
    @DisplayName("Customer cannot update own role")
    void customerCannotUpdateOwnRole() throws Exception{
        User customer = createCustomer();

        String loginResponse = login(customer);
        String accessToken = extractToken(loginResponse, "accessToken");

        UpdateUserRoleRequestDTO request = new UpdateUserRoleRequestDTO();
        request.setRole(UserRole.ROLE_ADMIN);

        mockMvc.perform(patch("/users/" + customer.getId() + "/role")
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Customer cannot update other user's role")
    void customerCannotUpdateOthersRole() throws Exception{
        User customer1 = createCustomer();
        User customer2 = createCustomer();

        String loginResponse = login(customer1);
        String accessToken = extractToken(loginResponse, "accessToken");

        UpdateUserRoleRequestDTO request = new UpdateUserRoleRequestDTO();
        request.setRole(UserRole.ROLE_ADMIN);

        mockMvc.perform(patch("/users/" + customer2.getId() + "/role")
                        .header("Authorization", authHeader(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
