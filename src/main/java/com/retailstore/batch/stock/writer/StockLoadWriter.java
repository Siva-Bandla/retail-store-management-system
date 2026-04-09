//package com.retailstore.batch.stock.writer;
//
//import com.retailstore.batch.stock.model.StockReconciliationDTO;
//import com.retailstore.batch.stock.repository.TempStockRepository;
//import org.springframework.batch.item.Chunk;
//import org.springframework.batch.item.ItemWriter;
//import org.springframework.stereotype.Component;
//
//@Component
//public class StockLoadWriter implements ItemWriter<StockReconciliationDTO> {
//
//    private final TempStockRepository tempStockRepository;
//
//    public StockLoadWriter(TempStockRepository tempStockRepository) {
//        this.tempStockRepository = tempStockRepository;
//    }
//
//    @Override
//    public void write(Chunk<? extends StockReconciliationDTO> chunk) throws Exception {
//        tempStockRepository.saveAll(chunk.getItems());
//    }
//}
