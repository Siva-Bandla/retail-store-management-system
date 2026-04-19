package com.retailstore.order.mapper;

import com.retailstore.order.dto.OrderItemResponseDTO;
import com.retailstore.order.dto.OrderResponseDTO;
import com.retailstore.order.entity.Order;
import com.retailstore.order.entity.OrderItem;
import com.retailstore.payment.entity.Payment;
import com.retailstore.payment.enums.PaymentMethod;

import java.util.List;

/**
 * Utility mapper class responsible for converting {@link Order} entities
 * into {@link OrderResponseDTO} objects.
 *
 * <p>This mapper transforms the Order entity and its associated
 * {@link com.retailstore.order.entity.OrderItem} entities into response DTOs
 * suitable for returning through the API layer.</p>
 *
 * <p>The mapping includes:</p>
 * <ul>
 *     <li>Order basic details (orderId, userId, totalAmount, status, createdAt)</li>
 *     <li>List of ordered items with product details</li>
 * </ul>
 *
 * <p>This class contains only static utility methods and should not be instantiated.</p>
 */
public class OrderMapper {

    private OrderMapper(){}

    public static OrderResponseDTO mapToOrderResponseDTO(Order order, List<OrderItem> orderItems){

        List<OrderItemResponseDTO> responseItems = orderItems.stream()
                .map(orderItem -> OrderItemResponseDTO.builder()
                        .productId(orderItem.getProductId())
                        .productName(orderItem.getProductName())
                        .price(orderItem.getPrice())
                        .quantity(orderItem.getQuantity())
                        .build()).toList();

        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .items(responseItems)
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }

    public static OrderResponseDTO mapToOrderResponseDTO(Order order, List<OrderItem> orderItems,
                                                         PaymentMethod paymentMethod){

        List<OrderItemResponseDTO> responseItems = orderItems.stream()
                .map(orderItem -> OrderItemResponseDTO.builder()
                        .productId(orderItem.getProductId())
                        .productName(orderItem.getProductName())
                        .price(orderItem.getPrice())
                        .quantity(orderItem.getQuantity())
                        .build()).toList();

        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .paymentMethod(paymentMethod)
                .items(responseItems)
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
