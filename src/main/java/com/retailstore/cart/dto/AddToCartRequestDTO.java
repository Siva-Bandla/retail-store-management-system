package com.retailstore.cart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = false)
public class AddToCartRequestDTO {

    private Long userId;
    private Long productId;
    private Integer quantity;
}
