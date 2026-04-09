package com.retailstore.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderItemRequestDTO {

    @NotNull
    private Long productId;

    @Positive
    private Integer quantity;
}
