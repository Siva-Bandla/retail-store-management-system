package com.retailstore.batch.product.config;

import com.retailstore.batch.product.model.ProductUploadDTO;
import com.retailstore.batch.product.processor.ProductUploadProcessor;
import com.retailstore.batch.product.writer.ProductUploadWriter;
import com.retailstore.logging.BatchJobLoggerListener;
import com.retailstore.logging.BatchStepLoggerListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class ProductUploadBatchConfig {

    @Bean
    public Step productUploadStep(JobRepository jobRepository, PlatformTransactionManager txManager,
                                  @Qualifier("productItemReader") FlatFileItemReader<ProductUploadDTO> reader,
                                  ProductUploadProcessor processor, ProductUploadWriter writer,
                                  BatchStepLoggerListener stepLoggerListener) {

        return new StepBuilder("productUploadStep", jobRepository)
                .<ProductUploadDTO, ProductUploadDTO>chunk(20, txManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener(writer)
                .listener(stepLoggerListener)
                .build();
    }

    @Bean
    public Job productUploadJob(JobRepository jobRepository, Step productUploadStep,
                                BatchJobLoggerListener batchJobLoggerListener) {

        return new JobBuilder("productUploadJob", jobRepository)
                .start(productUploadStep)
                .listener(batchJobLoggerListener)
                .build();
    }
}
