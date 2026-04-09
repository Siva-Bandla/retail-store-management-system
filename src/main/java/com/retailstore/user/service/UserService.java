package com.retailstore.user.service;

import com.retailstore.user.dto.DeletedUserAndAddressResponseDTO;
import com.retailstore.user.dto.UpdateUserRequestDTO;
import com.retailstore.user.dto.UserRegisterRequestDTO;
import com.retailstore.user.dto.UserResponseDTO;
import com.retailstore.user.enums.UserRole;

import java.util.List;

public interface UserService {

    UserResponseDTO registerUser(UserRegisterRequestDTO userRegisterRequestDTO);
    UserResponseDTO getUserById(Long userId);
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO updateUser(Long userId, UpdateUserRequestDTO updateUserRequestDTO);
    DeletedUserAndAddressResponseDTO deleteUser(Long userId);
    UserResponseDTO updateUserRole(Long userId, UserRole userRole);
}
