package com.retailstore.payment.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum PaymentMethod {

    UPI("upi"),
    CARD("card"),
    NET_BANKING("net_banking"),
    WALLET("wallet");

    private final String value;

    PaymentMethod(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue(){
        return value;
    }

    @JsonCreator
    public static PaymentMethod fromValue(String value){

        if (value == null)
            return null;

        return Arrays.stream(PaymentMethod.values())
                .filter(method -> method.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid payment method: " + value +
                        ". Allowed values: " + Arrays.stream(PaymentMethod.values())
                        .map(PaymentMethod::getValue)
                        .toList()
                ));
    }
}
