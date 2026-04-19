package com.retailstore.batch.product.reader;

import com.retailstore.batch.product.model.ProductUploadDTO;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

@Component
public class ProductCSVReader {

    @Bean
    @StepScope
    public FlatFileItemReader<ProductUploadDTO> productItemReader(@Value("#{jobParameters['filePath']}") String filePath){

        return new FlatFileItemReaderBuilder<ProductUploadDTO>()
                .name("productCSVReader")
                .resource(new FileSystemResource(filePath))
                .delimited()
                .names("id", "categoryId", "name", "description", "price", "quantity", "imageUrl")
                .targetType(ProductUploadDTO.class)
                .linesToSkip(1)
                .build();
    }
}
