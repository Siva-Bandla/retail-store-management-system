package com.retailstore.batch.product.util;

import com.retailstore.batch.product.model.ProductUploadDTO;
import org.springframework.stereotype.Service;

import java.net.URL;

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

        if (dto.getQuantity() == null || dto.getQuantity() < 0){
            throw new IllegalArgumentException("Stock must be >= 0 for product: " + productId);
        }

        // Validate image URL if provided
        if (dto.getImageUrl() != null && !dto.getImageUrl().trim().isEmpty()) {
            validateImageUrl(dto.getImageUrl().trim(), productId);
        }
    }

    private void validateImageUrl(String imageUrl, Long productId) {
        try {
            new URL(imageUrl); // Check if it's a valid URL format
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid image URL format for product: " + productId);
        }
        // Actual image validation happens in ImageDownloadService via content-type and magic bytes
        // No need to enforce file extension here
    }
}