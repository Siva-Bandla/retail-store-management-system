//package com.retailstore.batch.stock.writer;
//
//import com.retailstore.batch.stock.model.StockReconciliationDTO;
//import com.retailstore.batch.stock.util.StockCSVGenerator;
//import com.retailstore.batch.stock.util.StockReconciliationService;
//import com.retailstore.batch.util.EmailSender;
//import org.springframework.batch.core.StepContribution;
//import org.springframework.batch.core.scope.context.ChunkContext;
//import org.springframework.batch.core.step.tasklet.Tasklet;
//import org.springframework.batch.repeat.RepeatStatus;
//import org.springframework.stereotype.Component;
//
//import java.io.File;
//import java.util.List;
//
//@Component
//public class StockReportWriter1 implements Tasklet {
//
//    private final StockCSVGenerator generator;
//    private final EmailSender emailSender;
//
//    public StockReportWriter1(StockCSVGenerator generator, EmailSender emailSender) {
//        this.generator = generator;
//        this.emailSender = emailSender;
//    }
//
//    @Override
//    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
//
//        List<StockReconciliationDTO> mismatches = StockReconciliationService.mismatches;
//
//        try {
//            File csv = generator.generateCSV(mismatches);
//            emailSender.sendReport("Stock Reconciliation Report",
//                    "Please find attached stock reconciliation mismatch report.", csv);
//
//        }catch (Exception exception) {
//            throw new RuntimeException(exception);
//        }
//
//        return RepeatStatus.FINISHED;
//    }
//}
