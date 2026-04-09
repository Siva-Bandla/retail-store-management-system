package com.retailstore.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.retailstore.payment.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = false)
public class PaymentRequestDTO {

    @NotNull
    private Long orderId;

    @NotNull
    private PaymentMethod paymentMethod;
}
