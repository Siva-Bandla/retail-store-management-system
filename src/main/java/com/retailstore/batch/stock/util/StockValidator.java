package com.retailstore.batch.stock.util;

import com.retailstore.batch.stock.model.StockReconciliationDTO;
import org.springframework.stereotype.Service;

@Service
public class StockValidator {

    public void validate(StockReconciliationDTO dto) {
        Long productId = dto.getProductId();

        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Invalid product ID: " + productId);
        }

        if (dto.getActualStock() == null || dto.getActualStock() < 0) {
            throw new IllegalArgumentException("Stock must be >= 0 for product: " + productId);
        }
    }
}
