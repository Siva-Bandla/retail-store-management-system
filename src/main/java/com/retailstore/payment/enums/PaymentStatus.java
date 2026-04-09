package com.retailstore.payment.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum PaymentStatus {

    PENDING("pending"),
    SUCCESS("success"),
    FAILED("failed"),
    REFUNDED("refunded");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue(){
        return value;
    }

    @JsonCreator
    public static PaymentStatus fromValue(String value){

        if (value == null)
            return null;

        return Arrays.stream(PaymentStatus.values())
                .filter(status -> status.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid payment status: " + value +
                        ". Allowed values: " + Arrays.stream(PaymentStatus.values())
                        .map(PaymentStatus::getValue)
                        .toList()
                ));
    }
}
