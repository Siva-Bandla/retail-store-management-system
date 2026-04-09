package com.retailstore.inventory.service;

import com.retailstore.inventory.dto.InventoryRequestDTO;
import com.retailstore.inventory.dto.InventoryResponseDTO;

import java.util.List;

public interface InventoryService {
    InventoryResponseDTO createInventory(InventoryRequestDTO inventoryRequestDTO);
    InventoryResponseDTO updateInventory(Long inventoryId, InventoryRequestDTO inventoryRequestDTO);
    List<InventoryResponseDTO> getAllInventories();
    InventoryResponseDTO getInventoryByProductId(Long productId);
    InventoryResponseDTO deactivateInventory(Long inventoryId);
}
