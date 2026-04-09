package com.retailstore.batch.order.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderReportDTO {

    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private LocalDateTime orderDate;
}
