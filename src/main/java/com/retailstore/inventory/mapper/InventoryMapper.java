package com.retailstore.inventory.mapper;

import com.retailstore.inventory.dto.InventoryResponseDTO;
import com.retailstore.inventory.entity.Inventory;

/**
 * Utility class responsible for mapping {@link Inventory} entities
 * to {@link InventoryResponseDTO} objects.
 *
 * <p>This mapper is used to convert persistence layer objects (entities)
 * into Data Transfer Objects (DTOs) that are returned in API responses.
 * This helps decouple the internal database structure from the API contract.</p>
 *
 * <p>The class has a private constructor to prevent instantiation since
 * it only contains static utility methods.</p>
 */
public class InventoryMapper {

    private InventoryMapper(){}

    public static InventoryResponseDTO mapToInventoryResponseDTO(Inventory inventory){

        return InventoryResponseDTO.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .stock(inventory.getStock())
                .deleted(inventory.getDeleted())
                .build();
    }
}
