package com.retailstore.batch.stock.writer;

import com.retailstore.batch.stock.model.StockReconciliationView;
import com.retailstore.batch.stock.repository.StockReconciliationViewRepository;
import com.retailstore.batch.stock.util.StockCSVGenerator;
import com.retailstore.batch.util.EmailSender;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class StockReportWriter implements Tasklet {

    private final StockReconciliationViewRepository viewRepository;
    private final StockCSVGenerator generator;
    private final EmailSender emailSender;

    public StockReportWriter(StockReconciliationViewRepository viewRepository,
                              StockCSVGenerator generator, EmailSender emailSender) {
        this.viewRepository = viewRepository;
        this.generator = generator;
        this.emailSender = emailSender;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        List<StockReconciliationView> rows = viewRepository.findAll();

        try {
            File csv = generator.generateCSV(rows);
            emailSender.sendReport("Stock Reconciliation Report",
                    "Please find attached the stock reconciliation summary.", csv);

        }catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        return RepeatStatus.FINISHED;
    }
}
