package com.retailstore.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.retailstore.order.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = false)
public class UpdateOrderStatusRequestDTO {

    @NotNull(message = "Status must be provided")
    private OrderStatus status;
}
