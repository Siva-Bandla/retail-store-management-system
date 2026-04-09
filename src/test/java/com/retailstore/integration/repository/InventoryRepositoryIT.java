package com.retailstore.integration.repository;

import com.retailstore.inventory.entity.Inventory;
import com.retailstore.inventory.repository.InventoryRepository;
import com.retailstore.testdata.InventoryTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(InventoryTestData.class)
class InventoryRepositoryIT {

    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private InventoryTestData inventoryTestData;

    // =================<< FIND BY PRODUCT ID >>======================
    @Test
    @DisplayName("findByProductId returns inventory when exists")
    void findByProductId_exists() {

        Inventory inventory = inventoryTestData.createInventory(101L, 50);

        Optional<Inventory> found = inventoryRepository.findByProductId(101L);

        assertThat(found).isPresent();
        assertThat(found.get().getStock()).isEqualTo(50);
    }

    @Test
    @DisplayName("findByProductId returns empty when not exists")
    void findByProductId_notExists() {

        Optional<Inventory> found = inventoryRepository.findByProductId(999L);

        assertThat(found).isEmpty();
    }

    // =================<< FIND BY PRODUCT IDS AND DELETED = FALSE >>======================
    @Test
    @DisplayName("findByProductIdInAndDeletedFalse returns only active inventories")
    void findByProductIdInAndDeletedFalse_activeOnly() {

        Inventory i1 = inventoryTestData.createInventory(201L, 10);
        Inventory i2 = inventoryTestData.createInventory(202L, 20);

        Inventory deleted = inventoryTestData.createInventory(203L, 30);
        deleted.setDeleted(true);
        inventoryRepository.save(deleted);

        List<Inventory> list = inventoryRepository.findByProductIdInAndDeletedFalse(
                List.of(201L, 202L, 203L)
        );

        assertThat(list)
                .hasSize(2)
                .extracting(Inventory::getProductId)
                .containsExactlyInAnyOrder(201L, 202L);
    }

    // =================<< EXISTS BY PRODUCT ID AND DELETED = FALSE >>======================
    @Test
    @DisplayName("existsByProductIdAndDeletedFalse returns true when active")
    void existsByProductIdAndDeletedFalse_true() {

        inventoryTestData.createInventory(301L, 40);

        boolean exists = inventoryRepository.existsByProductIdAndDeletedFalse(301L);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByProductIdAndDeletedFalse returns false when deleted")
    void existsByProductIdAndDeletedFalse_deleted() {

        Inventory inv = inventoryTestData.createInventory(302L, 20);
        inv.setDeleted(true);
        inventoryRepository.save(inv);

        boolean exists = inventoryRepository.existsByProductIdAndDeletedFalse(302L);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByProductIdAndDeletedFalse returns false when not exists")
    void existsByProductIdAndDeletedFalse_notExists() {

        boolean exists = inventoryRepository.existsByProductIdAndDeletedFalse(999L);

        assertThat(exists).isFalse();
    }

    // =================<< FIND BY ID AND DELETED = FALSE >>======================
    @Test
    @DisplayName("findByIdAndDeletedFalse returns record when active")
    void findByIdAndDeletedFalse_active() {

        Inventory inv = inventoryTestData.createInventory(401L, 60);

        Optional<Inventory> found = inventoryRepository.findByIdAndDeletedFalse(inv.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getProductId()).isEqualTo(401L);
    }

    @Test
    @DisplayName("findByIdAndDeletedFalse returns empty when deleted")
    void findByIdAndDeletedFalse_deleted() {

        Inventory inv = inventoryTestData.createInventory(402L, 80);
        inv.setDeleted(true);
        inventoryRepository.save(inv);

        Optional<Inventory> found = inventoryRepository.findByIdAndDeletedFalse(inv.getId());

        assertThat(found).isEmpty();
    }

    // =================<< FIND BY PRODUCT ID AND DELETED = FALSE >>======================
    @Test
    @DisplayName("findByProductIdAndDeletedFalse returns inventory when active")
    void findByProductIdAndDeletedFalse_active() {

        inventoryTestData.createInventory(501L, 70);

        Optional<Inventory> found = inventoryRepository.findByProductIdAndDeletedFalse(501L);

        assertThat(found).isPresent();
        assertThat(found.get().getStock()).isEqualTo(70);
    }

    @Test
    @DisplayName("findByProductIdAndDeletedFalse returns empty when deleted")
    void findByProductIdAndDeletedFalse_deleted() {

        Inventory inv = inventoryTestData.createInventory(502L, 90);
        inv.setDeleted(true);
        inventoryRepository.save(inv);

        Optional<Inventory> found = inventoryRepository.findByProductIdAndDeletedFalse(502L);

        assertThat(found).isEmpty();
    }
}