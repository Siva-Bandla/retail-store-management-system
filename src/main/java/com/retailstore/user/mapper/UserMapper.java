package com.retailstore.user.mapper;

import com.retailstore.user.dto.UserResponseDTO;
import com.retailstore.user.entity.User;

/**
 * Utility class for mapping {@link User} entities to
 * {@link UserResponseDTO} objects used in API responses.
 *
 * <p>Provides static methods to convert User entities into DTOs,
 * ensuring only necessary user information is exposed to clients.</p>
 */
public class UserMapper {

    public static UserResponseDTO mapToUserResponseDTO(User user){

        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
