package com.retailstore.user.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum UserRole {

    ROLE_ADMIN("admin"),
    ROLE_CUSTOMER("customer"),
    ROLE_SUPPLIER("supplier");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static UserRole fromValue(String value){

        if (value == null)
            return null;

        return Arrays.stream(UserRole.values())
                .filter(role -> role.getValue().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid user role: " + value +
                        ". Allowed values: " + Arrays.stream(UserRole.values())
                        .map(UserRole::getValue)
                        .toList()
                ));
    }
}
