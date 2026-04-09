package com.retailstore.testdata;

import com.retailstore.inventory.entity.Inventory;
import com.retailstore.inventory.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class InventoryTestData {

    @Autowired
    private InventoryRepository inventoryRepository;

    public Inventory createInventory(Long productId, int stock){
        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setStock(stock);

        return inventoryRepository.save(inventory);
    }
}
