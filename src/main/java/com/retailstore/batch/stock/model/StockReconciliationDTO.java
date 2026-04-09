package com.retailstore.batch.stock.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReconciliationDTO {

    private Long productId;
    private Integer actualStock;
}
