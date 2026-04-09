package com.retailstore.product.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = false)
public class ProductRequestDTO {

    @NotBlank
    private String name;

    private String description;

    @Positive
    private BigDecimal price;

    @Positive
    private Integer quantity;

    @NotNull
    private Long categoryId;
}
