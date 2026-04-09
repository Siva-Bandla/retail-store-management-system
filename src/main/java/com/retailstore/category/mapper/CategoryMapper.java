package com.retailstore.category.mapper;

import com.retailstore.category.dto.CategoryResponseDTO;
import com.retailstore.category.entity.Category;

/**
 * Utility class responsible for converting {@link Category} entities
 * into {@link CategoryResponseDTO} objects.
 *
 * <p>This mapper helps in separating the persistence layer from the API layer
 * by transforming entity objects into Data Transfer Objects (DTOs) that are
 * safe to expose in API responses.</p>
 *
 * <p>All methods in this class are static because the mapper does not maintain
 * any state.</p>
 */
public class CategoryMapper {

    private CategoryMapper(){}

    public static CategoryResponseDTO mapToCategoryResponseDTO(Category category){

        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}
