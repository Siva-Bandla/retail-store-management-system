//package com.retailstore.batch.stock.writer;
//
//import com.retailstore.batch.stock.model.StockReconciliationDTO;
//import com.retailstore.batch.stock.util.StockCSVGenerator;
//import com.retailstore.batch.util.EmailSender;
//import com.retailstore.inventory.entity.Inventory;
//import com.retailstore.inventory.repository.InventoryRepository;
//import com.retailstore.product.repository.ProductRepository;
//import org.springframework.batch.core.ExitStatus;
//import org.springframework.batch.core.StepExecution;
//import org.springframework.batch.core.StepExecutionListener;
//import org.springframework.batch.item.Chunk;
//import org.springframework.batch.item.ItemWriter;
//import org.springframework.stereotype.Component;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//
//@Component
//public class StockReconciliationWriter implements ItemWriter<StockReconciliationDTO>, StepExecutionListener {
//
//    private final InventoryRepository inventoryRepository;
//    private final ProductRepository productRepository;
//
//    private final StockCSVGenerator csvGenerator;
//    private final EmailSender emailSender;
//
//    private final List<StockReconciliationDTO> allInventories = new ArrayList<>();
//
//    public StockReconciliationWriter(InventoryRepository inventoryRepository,
//                                     ProductRepository productRepository,
//                                     StockCSVGenerator csvGenerator,
//                                     EmailSender emailSender) {
//        this.inventoryRepository = inventoryRepository;
//        this.productRepository = productRepository;
//        this.csvGenerator = csvGenerator;
//        this.emailSender = emailSender;
//    }
//
//    @Override
//    public void write(Chunk<? extends StockReconciliationDTO> chunk) throws Exception {
//
//        for (StockReconciliationDTO dto: chunk.getItems()) {
//            try {
//                if (!productRepository.existsById(dto.getProductId())) {
//                    dto.setStatus("FAILED");
//                    dto.setMessage("Product not found");
//
//                    allInventories.add(dto);
//                    continue;
//                }
//
//                Inventory inventory = inventoryRepository.findByProductId(dto.getProductId())
//                        .orElseGet(() -> {
//                            Inventory inv = new Inventory();
//                            inv.setProductId(dto.getProductId());
//                            inv.setStock(0);
//                            return inv;
//                        });
//                inventory.setStock(dto.getActualStock());
//                inventory.setDeleted(false);
//
//                inventoryRepository.save(inventory);
//
//                dto.setStatus("UPDATED");
//                dto.setMessage("Stock set to " + dto.getActualStock());
//
//            }catch (Exception exception) {
//                dto.setStatus("FAILED");
//                dto.setMessage(exception.getMessage());
//            }
//
//            allInventories.add(dto);
//        }
//    }
//
//    @Override
//    public ExitStatus afterStep(StepExecution stepExecution) {
//        try {
//            File csv = csvGenerator.generateCSV(allInventories);
//            emailSender.sendReport("Stock Reconciliation Report",
//                    "Please find attached reconciliation report.", csv);
//            return ExitStatus.COMPLETED;
//
//        } catch (Exception exception) {
//            exception.printStackTrace();
//            return ExitStatus.FAILED;
//        }
//    }
//}
