package com.retailstore.batch.product.processor;

import com.retailstore.batch.product.model.ProductUploadDTO;
import com.retailstore.batch.product.util.ProductUploadValidator;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductUploadProcessor implements ItemProcessor<ProductUploadDTO, ProductUploadDTO> {

    @Autowired
    private ProductUploadValidator validator;

    @Override
    public ProductUploadDTO process(@NotNull ProductUploadDTO dto) throws Exception {
        validator.validate(dto);

        return dto;
    }
}
