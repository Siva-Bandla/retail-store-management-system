//package com.retailstore.batch.stock.util;
//
//import com.retailstore.batch.stock.model.StockReconciliationDTO;
//import com.retailstore.batch.stock.repository.TempStockRepository;
//import com.retailstore.inventory.entity.Inventory;
//import com.retailstore.inventory.repository.InventoryRepository;
//import org.springframework.batch.core.StepContribution;
//import org.springframework.batch.core.scope.context.ChunkContext;
//import org.springframework.batch.core.step.tasklet.Tasklet;
//import org.springframework.batch.repeat.RepeatStatus;
//
//import java.util.List;
//
//public class StockReconciliationService implements Tasklet {
//
//    private final TempStockRepository tempStockRepository;
//    private final InventoryRepository inventoryRepository;
//
//    public static List<StockReconciliationDTO> mismatches;
//
//    public StockReconciliationService(TempStockRepository tempStockRepository, InventoryRepository inventoryRepository) {
//        this.tempStockRepository = tempStockRepository;
//        this.inventoryRepository = inventoryRepository;
//    }
//
//    @Override
//    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
//
//        List<StockReconciliationDTO> tempRows = tempStockRepository.findAll();
//
//        mismatches = tempRows.stream()
//                .map(dto -> {
//                    Inventory inv = inventoryRepository.findByProductId(dto.getProductId())
//                            .orElse(null);
//
//                    if (inv == null) {
//                        dto.setStatus("FAILED");
//                        dto.setMessage("Product not found");
//                        return dto;
//                    }
//
//                    int systemStock = inv.getStock();
//                    int actualStock = dto.getActualStock();
//
//                    if (systemStock != actualStock) {
//                        dto.setStatus("MISMATCH");
//                        dto.setMessage("System=" + systemStock + ", Warehouse=" + actualStock);
//                        return dto;
//                    }
//
//                    dto.setStatus("MATCH");
//                    dto.setMessage("No difference");
//                    return dto;
//                })
//                .filter(dto -> !dto.getStatus().equals("MATCH"))
//                .toList();
//
//        return RepeatStatus.FINISHED;
//    }
//}
