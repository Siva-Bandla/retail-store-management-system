package com.retailstore.order.service;

import com.retailstore.order.dto.CreateOrderRequestDTO;
import com.retailstore.order.dto.OrderResponseDTO;
import com.retailstore.order.dto.UpdateOrderStatusRequestDTO;

import java.util.List;

public interface OrderService {

    OrderResponseDTO createOrder(CreateOrderRequestDTO createOrderRequestDTO);
    OrderResponseDTO getOrderById(Long orderId);
    List<OrderResponseDTO> getAllOrders();
    List<OrderResponseDTO> getOrdersByUser(Long userId);
    OrderResponseDTO updateOrderStatus(Long orderId, UpdateOrderStatusRequestDTO status);
    OrderResponseDTO cancelOrder(Long orderId);
    OrderResponseDTO createOrderFromCart(Long userId);
}
