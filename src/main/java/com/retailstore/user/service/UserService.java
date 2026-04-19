package com.retailstore.user.service;

import com.retailstore.user.dto.*;
import com.retailstore.user.enums.UserRole;

import java.util.List;

public interface UserService {

    UserResponseDTO registerUser(UserRegisterRequestDTO userRegisterRequestDTO);
    UserResponseDTO getUserById(Long userId);
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO updateUser(Long userId, UpdateUserRequestDTO updateUserRequestDTO);
    DeletedUserAndAddressResponseDTO deleteUser(Long userId);
    UserResponseDTO updateUserRole(Long userId, UserRole userRole);

    Boolean verifySecurityAnswer(SecurityVerifyRequestDTO securityVerifyRequestDTO);
    void resetPassword(ResetPasswordRequestDTO resetPasswordRequestDTO);
    void changePassword(Long userId, ChangePasswordRequestDTO changePasswordRequestDTO);
}
