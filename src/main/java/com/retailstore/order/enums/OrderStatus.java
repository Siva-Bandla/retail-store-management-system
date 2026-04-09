package com.retailstore.order.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum OrderStatus {

    CREATED("created"),
    PAID("paid"),
    SHIPPED("shipped"),
    DELIVERED("delivered"),
    CANCELLED("cancelled");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue(){
        return value;
    }

    @JsonCreator
    public static OrderStatus fromValue(String value){

        if (value == null)
            return null;

        return Arrays.stream(OrderStatus.values())
                .filter(status -> status.getValue().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid order status: " + value +
                        ". Allowed values: " + Arrays.stream(OrderStatus.values())
                        .map(OrderStatus::getValue)
                        .toList()
                ));
    }
}
