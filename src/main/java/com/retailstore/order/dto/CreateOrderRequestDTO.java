package com.retailstore.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = false)
public class CreateOrderRequestDTO {

    @NotNull
    private Long userId;

    @NotEmpty
    private List<OrderItemRequestDTO> items;
}
