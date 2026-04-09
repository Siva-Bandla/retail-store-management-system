package com.retailstore.user.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum AddressType {

    HOME("home"),
    OFFICE("office"),
    BILLING("billing"),
    SHIPPING("shipping"),
    OTHER("other");

    private final String value;

    AddressType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue(){
        return value;
    }

    @JsonCreator
    public static AddressType fromValue(String value){

        if (value == null)
            return null;

        return Arrays.stream(AddressType.values())
                .filter(type -> type.getValue().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid address type: " + value +
                        ". Allowed values: " + Arrays.stream(AddressType.values())
                        .map(AddressType::getValue)
                        .toList()
                ));
    }
}
