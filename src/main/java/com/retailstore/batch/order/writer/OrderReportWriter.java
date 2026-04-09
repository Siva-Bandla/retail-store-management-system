package com.retailstore.batch.order.writer;

import com.retailstore.batch.order.model.OrderReportDTO;
import com.retailstore.batch.order.util.OrderCSVGenerator;
import com.retailstore.batch.util.EmailSender;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderReportWriter implements ItemWriter<OrderReportDTO>, StepExecutionListener {

    private final List<OrderReportDTO> allOrders = new ArrayList<>();

    private final OrderCSVGenerator csvGenerator;
    private final EmailSender emailSender;

    public OrderReportWriter(OrderCSVGenerator csvGenerator, EmailSender emailSender) {
        this.csvGenerator = csvGenerator;
        this.emailSender = emailSender;
    }

    @Override
    public void write(Chunk<? extends OrderReportDTO> chunk) throws Exception {
        allOrders.addAll(chunk.getItems());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        try{
            File csv = csvGenerator.generateCSV(allOrders);
            emailSender.sendReport("Daily Order Report",
                    "Please find the attached the daily order report CSV file.", csv);
            return ExitStatus.COMPLETED;

        }catch (Exception exception){
            exception.printStackTrace();
            return ExitStatus.FAILED;
        }
    }
}
