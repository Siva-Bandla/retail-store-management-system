package com.retailstore.batch.stock.writer;

import com.retailstore.batch.stock.model.StockReconciliationView;
import com.retailstore.inventory.entity.Inventory;
import com.retailstore.inventory.repository.InventoryRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class StockMismatchWriter implements ItemWriter<StockReconciliationView> {

    private final InventoryRepository inventoryRepository;

    public StockMismatchWriter(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public void write(Chunk<? extends StockReconciliationView> chunk) throws Exception {

        chunk.forEach(view -> {
            if ("MISMATCH".equals(view.getStatus())) {

                Inventory inv = inventoryRepository.findByProductId(view.getProductId())
                        .orElseThrow();

                inv.setStock(view.getWarehouseStock());
                inventoryRepository.save(inv);
            }
        });
    }
}
