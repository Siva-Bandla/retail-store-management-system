//package com.retailstore.batch.stock.processor;
//
//import com.retailstore.batch.stock.model.StockReconciliationDTO;
//import com.retailstore.batch.stock.util.StockReconciliationService;
//import org.springframework.batch.item.ItemProcessor;
//import org.springframework.stereotype.Component;
//
//import java.util.Iterator;
//
//@Component
//public class StockUpdateProcessor implements ItemProcessor<StockReconciliationDTO, StockReconciliationDTO> {
//
//    private final Iterator<StockReconciliationDTO> iterator;
//
//    public StockUpdateProcessor() {
//        this.iterator = StockReconciliationService.mismatches == null ? null :
//                StockReconciliationService.mismatches.iterator();
//    }
//
//    @Override
//    public StockReconciliationDTO process(StockReconciliationDTO item) throws Exception {
//        if (iterator != null && iterator.hasNext()) {
//            return iterator.next();
//        }
//        return null;
//    }
//}
