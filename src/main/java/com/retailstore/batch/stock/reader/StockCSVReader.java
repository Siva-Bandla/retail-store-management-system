package com.retailstore.batch.stock.reader;

import com.retailstore.batch.stock.model.StockReconciliationDTO;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

@Component
public class StockCSVReader {

    @Bean
    @StepScope
    public FlatFileItemReader<StockReconciliationDTO> stockItemReader(
            @Value("#{jobParameters['filePath']}") String filePath) {

        return new FlatFileItemReaderBuilder<StockReconciliationDTO>()
                .name("stockCSVReader")
                .resource(new FileSystemResource(filePath))
                .delimited()
                .names("productId", "actualStock")
                .targetType(StockReconciliationDTO.class)
                .linesToSkip(1)
                .build();
    }
}
