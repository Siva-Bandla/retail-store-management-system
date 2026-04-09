package com.retailstore.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = false)
public class UpdateUserRequestDTO {

    @NotBlank(message = "User name cannot be blank")
    @Size(max = 100, message = "User name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    private String password;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phone;
}
