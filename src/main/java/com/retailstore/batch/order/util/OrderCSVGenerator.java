package com.retailstore.batch.order.util;

import com.retailstore.batch.order.model.OrderReportDTO;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrderCSVGenerator {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public File generateCSV(List<OrderReportDTO> orders) throws Exception{

        String date = LocalDateTime.now().format(DTF);
        String folder = "reports/orders/";
        new File(folder).mkdirs();

        String filePath = folder + "orders-" + date + ".csv";
        File csv = new File(filePath);

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(csv))){

            writer.write("OrderId,UserId,Amount,OrderDate");
            writer.newLine();

            for (OrderReportDTO dto: orders){
                writer.write(dto.getOrderId() + "," +
                        dto.getUserId() + "," +
                        dto.getAmount() + "," +
                        dto.getOrderDate());
                writer.newLine();
            }
        }
        return csv;
    }
}
