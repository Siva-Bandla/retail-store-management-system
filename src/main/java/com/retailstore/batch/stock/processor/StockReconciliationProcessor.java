package com.retailstore.batch.stock.processor;

import com.retailstore.batch.stock.model.StockReconciliationDTO;
import com.retailstore.batch.stock.model.TempStock;
import com.retailstore.batch.stock.util.StockValidator;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StockReconciliationProcessor implements ItemProcessor<StockReconciliationDTO, TempStock> {

    @Autowired
    private StockValidator validator;

    @Override
    public TempStock process(@NotNull StockReconciliationDTO dto) throws Exception {
        validator.validate(dto);

        TempStock entity = new TempStock();
        entity.setProductId(dto.getProductId());
        entity.setActualStock(dto.getActualStock());

        return entity;
    }
}
