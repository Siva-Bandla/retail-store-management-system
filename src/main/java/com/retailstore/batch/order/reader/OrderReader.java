package com.retailstore.batch.order.reader;

import com.retailstore.order.entity.Order;
import com.retailstore.order.repository.OrderRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class OrderReader {

    @Autowired
    private OrderRepository orderRepository;

    @Bean
    @StepScope
    public RepositoryItemReader<Order> orderRepositoryReader() {

        return new RepositoryItemReaderBuilder<Order>()
                .repository(orderRepository)
                .methodName("findByCreatedAtAfter")
                .arguments(List.of(LocalDateTime.now().minusHours(24)))
                .pageSize(20)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .name("orderRepositoryReader")
                .build();
    }
}
