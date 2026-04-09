package com.retailstore.inventory.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = false)
public class InventoryRequestDTO {

    @NotNull
    private Long productId;

    @Positive
    private Integer stock;
}
