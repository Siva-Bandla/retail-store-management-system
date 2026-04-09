package com.retailstore.testdata;

import com.retailstore.order.entity.Order;
import com.retailstore.order.entity.OrderItem;
import com.retailstore.order.enums.OrderStatus;
import com.retailstore.order.repository.OrderItemRepository;
import com.retailstore.order.repository.OrderRepository;
import com.retailstore.product.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

public class OrderTestData {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    public Order createOrder(Long userId, double total) {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(BigDecimal.valueOf(total));

        return orderRepository.save(order);
    }

    public OrderItem createOrderItem(Long orderId, Product product, int qty){
        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(product.getId());
        orderItem.setProductName(product.getName());
        orderItem.setPrice(product.getPrice());
        orderItem.setQuantity(qty);
        orderItem.setOrderId(orderId);

        return orderItemRepository.save(orderItem);
    }
}