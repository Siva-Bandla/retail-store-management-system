package com.retailstore.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.retailstore.user.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = false)
public class UpdateUserRoleRequestDTO {

    @NotNull(message = "Role should be provided")
    private UserRole role;
}
