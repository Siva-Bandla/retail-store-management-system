package com.retailstore.inventory.repository;

import com.retailstore.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    List<Inventory> findByProductIdInAndDeletedFalse(List<Long> productIds);

    boolean existsByProductIdAndDeletedFalse(Long productId);

    Optional<Inventory> findByIdAndDeletedFalse(Long inventoryId);

    Optional<Inventory> findByProductIdAndDeletedFalse(Long productId);

}
