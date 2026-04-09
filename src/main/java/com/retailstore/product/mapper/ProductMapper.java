package com.retailstore.product.mapper;

import com.retailstore.product.dto.ProductResponseDTO;
import com.retailstore.product.entity.Product;

/**
 * Utility class used to convert {@link Product} entity objects
 * into {@link ProductResponseDTO} objects.
 *
 * <p>This mapper is responsible for transforming the Product entity
 * retrieved from the database into a response DTO that can be safely
 * returned to clients via REST APIs.</p>
 *
 * <p>This class is designed as a utility class and therefore has
 * a private constructor to prevent instantiation.</p>
 */
public class ProductMapper {

    private ProductMapper(){}

    public static ProductResponseDTO mapToProductResponseDTO(Product product, int stock){

        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(stock)
                .categoryId(product.getCategoryId())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
