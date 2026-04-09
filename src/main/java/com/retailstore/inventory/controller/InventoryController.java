package com.retailstore.inventory.controller;

import com.retailstore.inventory.dto.InventoryRequestDTO;
import com.retailstore.inventory.dto.InventoryResponseDTO;
import com.retailstore.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller responsible for handling HTTP requests related to
 * inventory management.
 *
 * <p>This controller exposes endpoints for creating, updating
 * retrieving, fetch by product, deactivating inventory</p>
 *
 * <p>The controller delegates business logic to {@link InventoryService}
 * and returns {@link InventoryResponseDTO} objects as API responses.</p>
 *
 * <p>All endpoints follow RESTful design principles and return appropriate
 * HTTP status codes for successful and error responses.</p>
 */
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("inventories")
public class InventoryController {

    private final InventoryService inventoryService;
    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Creates a new inventory record for a product.
     *
     * <p>This endpoint accepts product ID and initial quantity,
     * validates the request, and creates an inventory entry
     * associated with the specified product.</p>
     *
     * @param inventoryRequestDTO request payload containing product ID
     *                            and initial stock quantity
     * @return {@link InventoryResponseDTO} representing the newly created inventory
     */
    @PostMapping
    public ResponseEntity<InventoryResponseDTO> createInventory(
            @Valid @RequestBody InventoryRequestDTO inventoryRequestDTO){

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.createInventory(inventoryRequestDTO));
    }

    /**
     * Updates the inventory quantity for a product.
     *
     * <p>This endpoint modifies the stock level of an existing inventory
     * by adding the specified quantity.</p>
     *
     * @param inventoryRequestDTO request payload containing product ID
     *                            and quantity to update
     * @return {@link InventoryResponseDTO} representing the updated inventory
     */
    @PutMapping("/{inventoryId}")
    public ResponseEntity<InventoryResponseDTO> updateInventory(
            @PathVariable Long inventoryId,
            @Valid @RequestBody InventoryRequestDTO inventoryRequestDTO){

        return ResponseEntity.status(HttpStatus.OK).body(inventoryService.updateInventory(
                inventoryId, inventoryRequestDTO));
    }

    /**
     * Retrieves all inventory records available in the system.
     *
     * @return a list of {@link InventoryResponseDTO} containing
     * inventory details for all products
     */
    @GetMapping
    public ResponseEntity<List<InventoryResponseDTO>> getAllInventories(){

        return ResponseEntity.ok(inventoryService.getAllInventories());
    }

    /**
     * Retrieves inventory information for a specific product.
     *
     * @param productId the ID of the product whose inventory is requested
     * @return {@link InventoryResponseDTO} representing the inventory details
     */
    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponseDTO> getInventoryByProductId(@PathVariable Long productId){

        return ResponseEntity.ok(inventoryService.getInventoryByProductId(productId));
    }

    /**
     * Deactivates an inventory record.
     *
     * <p>This operation performs a soft delete by marking the inventory
     * as inactive without permanently removing it from the database.</p>
     *
     * @param inventoryId the ID of the inventory to deactivate
     * @return {@link InventoryResponseDTO} representing the updated inventory state
     */
    @DeleteMapping("/{inventoryId}")
    public ResponseEntity<InventoryResponseDTO> deactivateInventory(@PathVariable Long inventoryId){

        return ResponseEntity.ok(inventoryService.deactivateInventory(inventoryId));
    }
}
