package com.retailstore.batch.stock.util;

import com.retailstore.batch.stock.model.StockReconciliationDTO;
import com.retailstore.batch.stock.model.StockReconciliationView;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class StockCSVGenerator {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public File generateCSV(List<StockReconciliationView> stocks) throws Exception {

        String date = LocalDateTime.now().format(DTF);
        String folder = "reports/stocks/";
        new File(folder).mkdirs();

        String filePath = folder + "stocks-reconciled" + date + ".csv";
        File csv = new File(filePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csv))) {

            writer.write("productId,warehouseStock,systemStock,stockDifference,status,message");
            writer.newLine();

            for (StockReconciliationView dto: stocks) {
                writer.write(dto.getProductId() + "," +
                        dto.getWarehouseStock() + "," +
                        dto.getSystemStock() + "," +
                        dto.getStockDifference() + "," +
                        dto.getStatus() + "," +
                        dto.getMessage());
                writer.newLine();
            }
        }
        return csv;
    }
}
