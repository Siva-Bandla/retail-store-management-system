package com.retailstore.batch.stock.writer;

import com.retailstore.batch.stock.repository.TempStockRepository;
import com.retailstore.batch.stock.model.TempStock;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class TempStockWriter implements ItemWriter<TempStock> {
    
    private final TempStockRepository tempStockRepository;

    public TempStockWriter(TempStockRepository tempStockRepository) {
        this.tempStockRepository = tempStockRepository;
    }

    @Override
    public void write(Chunk<? extends TempStock> chunk) throws Exception {
        tempStockRepository.saveAll(chunk.getItems());
    }
}
