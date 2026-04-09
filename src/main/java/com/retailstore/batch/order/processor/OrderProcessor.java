package com.retailstore.batch.order.processor;

import com.retailstore.batch.order.model.OrderReportDTO;
import com.retailstore.order.entity.Order;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class OrderProcessor implements ItemProcessor<Order, OrderReportDTO> {

    @Override
    public OrderReportDTO process(Order order) throws Exception {

        return new OrderReportDTO(
                order.getId(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getCreatedAt()
        );
    }
}
