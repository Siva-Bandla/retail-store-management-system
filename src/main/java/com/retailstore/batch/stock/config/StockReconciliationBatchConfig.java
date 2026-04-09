package com.retailstore.batch.stock.config;

import com.retailstore.batch.stock.model.StockReconciliationDTO;
import com.retailstore.batch.stock.model.StockReconciliationView;
import com.retailstore.batch.stock.model.TempStock;
import com.retailstore.batch.stock.processor.StockReconciliationProcessor;
import com.retailstore.batch.stock.repository.StockReconciliationViewRepository;
import com.retailstore.batch.stock.writer.StockMismatchWriter;
import com.retailstore.batch.stock.writer.StockReportWriter;
import com.retailstore.batch.stock.writer.TempStockTruncateTasklet;
import com.retailstore.batch.stock.writer.TempStockWriter;
import com.retailstore.logging.BatchJobLoggerListener;
import com.retailstore.logging.BatchStepLoggerListener;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class StockReconciliationBatchConfig {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Bean
    public Step truncateTempStockStep(JobRepository jobRepository, PlatformTransactionManager txManager,
                                      TempStockTruncateTasklet tasklet,
                                      BatchStepLoggerListener stepLoggerListener) {

        return new StepBuilder("truncateTempStockStep", jobRepository)
                .tasklet(tasklet, txManager)
                .listener(stepLoggerListener)
                .build();
    }

    @Bean
    public Step loadTempStockStep(JobRepository jobRepository, PlatformTransactionManager txManager,
                                  FlatFileItemReader<StockReconciliationDTO> reader,
                                  StockReconciliationProcessor processor, TempStockWriter writer,
                                  BatchStepLoggerListener stepLoggerListener) {

        return new StepBuilder("loadTempStockStep", jobRepository)
                .<StockReconciliationDTO, TempStock>chunk(20, txManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener(stepLoggerListener)
                .build();
    }

    @Bean
    public JpaPagingItemReader<StockReconciliationView> stockMismatchReader() {
        return new JpaPagingItemReaderBuilder<StockReconciliationView>()
                .name("diffReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT v FROM StockReconciliationView v")
                .pageSize(20)
                .build();
    }

    @Bean
    public Step stockMismatchStep(JobRepository jobRepository, PlatformTransactionManager txManager,
                                  StockMismatchWriter writer,
                                  BatchStepLoggerListener stepLoggerListener) {

        return new StepBuilder("stockMismatchStep", jobRepository)
                .<StockReconciliationView, StockReconciliationView>chunk(20, txManager)
                .reader(stockMismatchReader())
                .writer(writer)
                .listener(stepLoggerListener)
                .build();
    }

    @Bean
    public Step generateReportStep(JobRepository jobRepository, PlatformTransactionManager txManager,
                                   StockReportWriter tasklet,
                                   BatchStepLoggerListener stepLoggerListener) {

        return new StepBuilder("generateReportStep", jobRepository)
                .tasklet(tasklet, txManager)
                .listener(stepLoggerListener)
                .build();
    }

    @Bean
    public Job stockReconciliationJob(JobRepository jobRepository,
                                      Step truncateTempStockStep, Step loadTempStockStep,
                                      Step stockMismatchStep, Step generateReportStep,
                                      BatchJobLoggerListener batchJobLoggerListener) {

        return new JobBuilder("stockReconciliationJob", jobRepository)
                .start(truncateTempStockStep)
                .next(loadTempStockStep)
                .next(stockMismatchStep)
                .next(generateReportStep)
                .listener(batchJobLoggerListener)
                .build();
    }
}
