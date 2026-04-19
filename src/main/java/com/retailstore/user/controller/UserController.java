package com.retailstore.user.controller;

import com.retailstore.user.dto.*;
import com.retailstore.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller responsible for managing user-related operations.
 *
 * <p>This controller exposes endpoints for:</p>
 * <ul>
 *     <li>User registration</li>
 *     <li>User authentication (login)</li>
 *     <li>Retrieving user details</li>
 *     <li>Updating user information</li>
 *     <li>Updating user roles</li>
 *     <li>Deleting users along with associated addresses</li>
 * </ul>
 *
 * <p>All operations delegate the business logic to {@link UserService}.</p>
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registers a new user in the system.
     *
     * @param userRegisterRequestDTO DTO containing user registration details.
     * @return {@link UserResponseDTO} containing the created user information.
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody UserRegisterRequestDTO userRegisterRequestDTO){

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(userRegisterRequestDTO));
    }

    /**
     * Retrieves a user by their unique ID.
     *
     * @param userId the ID of the user.
     * @return {@link UserResponseDTO} containing the user details.
     */
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwnerByUserId(#userId, authentication)")
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long userId){

        return ResponseEntity.ok(userService.getUserById(userId));
    }

    /**
     * Retrieves all users available in the system.
     *
     * @return list of {@link UserResponseDTO} representing all users.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(){

        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Updates the details of an existing user.
     *
     * @param userId the ID of the user to update.
     * @param updateUserRequestDTO DTO containing updated user information.
     * @return {@link UserResponseDTO} containing the updated user details.
     */
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwnerByUserId(#userId, authentication)")
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long userId,
                                                      @Valid @RequestBody UpdateUserRequestDTO updateUserRequestDTO){
        return ResponseEntity.ok(userService.updateUser(userId, updateUserRequestDTO));
    }

    /**
     * Updates the role assigned to a user.
     *
     * @param userId the ID of the user.
     * @param roleRequest the new role to assign.
     * @return {@link UserResponseDTO} containing the updated user information.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{userId}/role")
    public ResponseEntity<UserResponseDTO> updateUserRole(@PathVariable Long userId,
                                                          @Valid @RequestBody UpdateUserRoleRequestDTO roleRequest){

        return ResponseEntity.ok(userService.updateUserRole(userId, roleRequest.getRole()));
    }

    /**
     * Deletes a user and all addresses associated with the user.
     *
     * @param userId the ID of the user to delete.
     * @return {@link DeletedUserAndAddressResponseDTO} containing
     *         the deleted user and associated addresses.
     */
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwnerByUserId(#userId, authentication)")
    @DeleteMapping("/{userId}")
    public ResponseEntity<DeletedUserAndAddressResponseDTO> deleteUser(@PathVariable Long userId){

        return ResponseEntity.ok(userService.deleteUser(userId));
    }

    @PostMapping("/verify-security-answer")
    public ResponseEntity<Map<String, Boolean>> verifySecurityAnswer(@RequestBody SecurityVerifyRequestDTO securityVerifyRequestDTO) {

        boolean result = userService.verifySecurityAnswer(securityVerifyRequestDTO);

        return ResponseEntity.ok(Map.of("verified", result));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequestDTO resetPasswordRequestDTO) {

        userService.resetPassword(resetPasswordRequestDTO);

        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }

    @PreAuthorize("@userSecurity.isOwnerByUserId(#userId, authentication)")
    @PutMapping("/{userId}/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable Long userId,
            @Valid @RequestBody ChangePasswordRequestDTO dto) {

        userService.changePassword(userId, dto);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}
