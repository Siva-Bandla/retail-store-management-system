package com.retailstore.order.dto;

import com.retailstore.order.enums.OrderStatus;
import com.retailstore.payment.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private Long orderId;
    private Long userId;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private List<OrderItemResponseDTO> items;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
