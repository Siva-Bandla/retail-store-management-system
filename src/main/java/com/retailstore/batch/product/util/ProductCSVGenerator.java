package com.retailstore.batch.product.util;

import com.retailstore.batch.product.model.ProductUploadDTO;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ProductCSVGenerator {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public File generateCSV(List<ProductUploadDTO> products) throws Exception {

        String date = LocalDateTime.now().format(DTF);
        String folder = "reports/products/";
        new File(folder).mkdirs();

        String filePath = folder + "products-uploaded-" + date + ".csv";
        File csv = new File(filePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csv))) {

            writer.write("id,categoryId,name,description,price,stock,status");
            writer.newLine();

            for (ProductUploadDTO dto: products) {
                writer.write(dto.getId() + "," +
                        dto.getCategoryId() + "," +
                        dto.getName() + "," +
                        dto.getDescription() + "," +
                        dto.getPrice() + "," +
                        dto.getStock() + "," +
                        dto.getStatus());
                writer.newLine();
            }
        }
        return csv;
    }
}
