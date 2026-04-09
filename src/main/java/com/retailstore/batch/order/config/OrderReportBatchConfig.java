package com.retailstore.batch.order.config;

import com.retailstore.batch.order.model.OrderReportDTO;
import com.retailstore.batch.order.processor.OrderProcessor;
import com.retailstore.batch.order.reader.OrderReader;
import com.retailstore.batch.order.writer.OrderReportWriter;
import com.retailstore.logging.BatchJobLoggerListener;
import com.retailstore.logging.BatchStepLoggerListener;
import com.retailstore.order.entity.Order;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class OrderReportBatchConfig {

    @Bean
    public Step orderReportStep(JobRepository jobRepository, PlatformTransactionManager txManager,
                                OrderReader reader, OrderProcessor processor, OrderReportWriter writer,
                                BatchStepLoggerListener stepLoggerListener) {

        return new StepBuilder("orderReportStep", jobRepository)
                .<Order, OrderReportDTO>chunk(20, txManager)
                .reader(reader.orderRepositoryReader())
                .processor(processor)
                .writer(writer)
                .listener(stepLoggerListener)
                .build();
    }

    @Bean
    public Job orderReportJob(JobRepository jobRepository, Step orderReportStep,
                              BatchJobLoggerListener batchJobLoggerListener) {

        return new JobBuilder("orderReportJob", jobRepository)
                .start(orderReportStep)
                .listener(batchJobLoggerListener)
                .build();
    }
}
