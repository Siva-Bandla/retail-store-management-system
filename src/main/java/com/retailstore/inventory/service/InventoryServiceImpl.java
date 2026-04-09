package com.retailstore.inventory.service;

import com.retailstore.exception.ResourceConflictException;
import com.retailstore.exception.ResourceNotFoundException;
import com.retailstore.inventory.dto.InventoryRequestDTO;
import com.retailstore.inventory.dto.InventoryResponseDTO;
import com.retailstore.inventory.entity.Inventory;
import com.retailstore.inventory.mapper.InventoryMapper;
import com.retailstore.inventory.repository.InventoryRepository;
import com.retailstore.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Service implementation responsible for handling all business operations
 * related to {@link Inventory} management.
 *
 * <p>This service provides functionality for:</p>
 * <ul>
 *     <li>Creating inventory records for products</li>
 *     <li>Updating inventory quantities</li>
 *     <li>Retrieving inventory details</li>
 *     <li>Fetching inventory by product</li>
 *     <li>Deactivating inventory records</li>
 * </ul>
 *
 * <p>Inventory operations include validation of product existence,
 * prevention of duplicate inventory records for the same product,
 * quantity validation to ensure inventory values remain consistent,
 * and mapping of {@link Inventory} entities to {@link InventoryResponseDTO}
 * objects for API responses.</p>
 *
 * <p>This service interacts with {@link InventoryRepository} and
 * {@link ProductRepository} to persist and retrieve inventory data.</p>
 */
@Service
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    @Autowired
    public InventoryServiceImpl(ProductRepository productRepository, InventoryRepository inventoryRepository){
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Creates a new inventory record for a given product.
     *
     * <p>This method validates that the product exists and that no inventory
     * already exists for it. It also ensures that the initial quantity is not negative.
     * If validation passes, a new inventory record is created and persisted.</p>
     *
     * @param inventoryRequestDTO  the quantity of inventoryRequestDTO to add to stock (must be > 0)
     * @return the newly created {@link Inventory} entity
     * @throws ResourceNotFoundException if the product with the given ID does not exist
     * @throws IllegalArgumentException  if the initial quantity is negative or inventory already exists
     */
    @Override
    @Transactional
    public InventoryResponseDTO createInventory(InventoryRequestDTO inventoryRequestDTO) {

        if (inventoryRequestDTO.getStock() < 0)
                throw new IllegalArgumentException("Initial quantity cannot be negative");

        if (inventoryRepository.existsByProductIdAndDeletedFalse(inventoryRequestDTO.getProductId()))
            throw new ResourceConflictException(
                    "Inventory already exists for product id: " +inventoryRequestDTO.getProductId()
            );

        Inventory inventory = new Inventory();
        inventory.setProductId(inventoryRequestDTO.getProductId());
        inventory.setStock(inventoryRequestDTO.getStock());

        Inventory savedInventory = inventoryRepository.save(inventory);

        return InventoryMapper.mapToInventoryResponseDTO(savedInventory);
    }

    /**
     * Updates the inventory quantity for a given product.
     *
     * @param inventoryId the ID of the inventory
     * @param inventoryRequestDTO  the quantity of inventoryRequestDTO to add to stock (must be > 0)
     * @return the updated {@link Inventory} entity
     * @throws ResourceNotFoundException if the inventory for the product does not exist or is already deleted
     * @throws IllegalArgumentException  if the quantity is not greater than 0
     */
    @Override
    @Transactional
    public InventoryResponseDTO updateInventory(Long inventoryId, InventoryRequestDTO inventoryRequestDTO) {

        Inventory inventory = inventoryRepository.findByIdAndDeletedFalse(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found with the product id: " + inventoryId
                ));

        if (!inventory.getProductId().equals(inventoryRequestDTO.getProductId())) {
            throw new ResourceConflictException(
                    "Inventory conflict: the existing inventory record is associated with product id "
                            + inventory.getProductId() + " and cannot be updated for product id "
                            + inventoryRequestDTO.getProductId()
            );
        }

        if (inventoryRequestDTO.getStock() <= 0)  throw new IllegalArgumentException("Quantity must be greater than 0");

        int updatedQuantity = inventory.getStock() + inventoryRequestDTO.getStock();

        inventory.setStock(updatedQuantity);

        Inventory updatedInventory = inventoryRepository.save(inventory);

        return InventoryMapper.mapToInventoryResponseDTO(updatedInventory);
    }

    /**
     * Retrieves all inventory records from the database.
     *
     * <p>Each {@link Inventory} entity is mapped to {@link InventoryResponseDTO}.</p>
     *
     * @return a {@link List} of {@link InventoryResponseDTO} representing all inventories
     */
    @Override
    public List<InventoryResponseDTO> getAllInventories() {

        List<Inventory> inventories = inventoryRepository.findAll();

        return inventories.stream()
                .map(InventoryMapper::mapToInventoryResponseDTO)
                .toList();
    }

    /**
     * Retrieves the inventory associated with a specific product.
     *
     * <p>First, validates that the product exists. Then, fetches the inventory
     * for that product. Throws {@link ResourceNotFoundException} if either
     * the product or inventory is not found.</p>
     *
     * @param productId the ID of the product whose inventory is to be retrieved
     * @return {@link InventoryResponseDTO} representing the inventory for the product
     * @throws ResourceNotFoundException if the product or inventory does not exist or is already deleted
     */
    @Override
    public InventoryResponseDTO getInventoryByProductId(Long productId) {

        if (!productRepository.existsByIdAndDeletedFalse(productId)){
            throw new ResourceNotFoundException(
                    "Product not found with id: " + productId
            );
        }

        Inventory inventory = inventoryRepository.findByProductIdAndDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found with the product id: " + productId
                ));

        return InventoryMapper.mapToInventoryResponseDTO(inventory);
    }

    /**
     * Deactivates an inventory by marking it as deleted.
     *
     * <p>The method ensures that only existing, non-deleted inventories
     * can be deactivated. The updated inventory is persisted in the database
     * and mapped to {@link InventoryResponseDTO}.</p>
     *
     * @param inventoryId the ID of the inventory to deactivate
     * @return {@link InventoryResponseDTO} representing the deactivated inventory
     * @throws ResourceNotFoundException if the inventory does not exist or is already deleted
     */
    @Override
    @Transactional
    public InventoryResponseDTO deactivateInventory(Long inventoryId) {

        Inventory inventory = inventoryRepository.findByIdAndDeletedFalse(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found with id: " + inventoryId
                ));
        if (productRepository.existsByIdAndDeletedFalse(inventory.getProductId())){
            throw new ResourceConflictException("Cannot delete the inventory associated with products");
        }
        inventory.setDeleted(true);

        Inventory deactivatedInventory = inventoryRepository.save(inventory);

        return InventoryMapper.mapToInventoryResponseDTO(deactivatedInventory);
    }
}
