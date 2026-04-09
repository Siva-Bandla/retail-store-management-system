package com.retailstore.batch.product.util;

import com.retailstore.batch.product.model.ProductUploadDTO;
import org.springframework.stereotype.Service;

@Service
public class ProductUploadValidator {

    public void validate(ProductUploadDTO dto){
        Long productId = dto.getId();

        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Invalid product ID: " + productId);
        }

        if (dto.getCategoryId() == null || dto.getCategoryId() <= 0) {
            throw new IllegalArgumentException("Category must be >= 0 for product: " + productId);
        }

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty for product: " + productId);
        }

        if (dto.getPrice() == null || dto.getPrice().doubleValue() <= 0){
            throw new IllegalArgumentException("Price must be > 0 for product: " + productId);
        }

        if (dto.getStock() == null || dto.getStock() < 0){
            throw new IllegalArgumentException("Stock must be >= 0 for product: " + productId);
        }
    }
}
