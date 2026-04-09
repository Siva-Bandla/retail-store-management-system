//package com.retailstore.batch.stock.writer;
//
//import com.retailstore.batch.stock.model.StockReconciliationDTO;
//import com.retailstore.inventory.entity.Inventory;
//import com.retailstore.inventory.repository.InventoryRepository;
//import org.springframework.batch.item.Chunk;
//import org.springframework.batch.item.ItemWriter;
//import org.springframework.stereotype.Component;
//
//@Component
//public class StockUpdateWriter implements ItemWriter<StockReconciliationDTO> {
//
//    private final InventoryRepository inventoryRepository;
//
//    public StockUpdateWriter(InventoryRepository inventoryRepository) {
//        this.inventoryRepository = inventoryRepository;
//    }
//
//    @Override
//    public void write(Chunk<? extends StockReconciliationDTO> chunk) throws Exception {
//
//        for (StockReconciliationDTO dto: chunk.getItems()) {
//            if (!"MISMATCH".equals(dto.getStatus())) continue;
//
//            Inventory inv = inventoryRepository.findByProductId(dto.getProductId())
//                    .orElseThrow();
//            inv.setStock(dto.getActualStock());
//            inventoryRepository.save(inv);
//
//            dto.setStatus("UPDATED");
//        }
//    }
//}
