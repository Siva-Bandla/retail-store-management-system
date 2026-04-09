package com.retailstore.auth.dto;

import lombok.Data;

@Data
public class AuthLoginRequestDTO {

    private String email;
    private String password;
}
