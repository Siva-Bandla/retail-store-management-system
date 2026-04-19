package com.retailstore.batch.product.processor;

import com.retailstore.batch.product.model.ProductUploadDTO;
import com.retailstore.batch.product.util.ImageDownloadService;
import com.retailstore.batch.product.util.ProductUploadValidator;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductUploadProcessor implements ItemProcessor<ProductUploadDTO, ProductUploadDTO> {

    @Autowired
    private ProductUploadValidator validator;

    @Autowired
    private ImageDownloadService imageDownloadService;

    @Override
    public ProductUploadDTO process(@NotNull ProductUploadDTO dto) throws Exception {
        validator.validate(dto);

        // Download and process image if URL is provided
        if (dto.getImageUrl() != null && !dto.getImageUrl().trim().isEmpty()) {
            String localImagePath = imageDownloadService.downloadImage(dto.getImageUrl().trim());
            dto.setImageUrl(localImagePath);
        } else {
            // Use placeholder if no image URL provided
            dto.setImageUrl("/images/placeholder.png");
        }

        return dto;
    }
}
