package com.retailstore.integration.repository;

import com.retailstore.order.entity.Order;
import com.retailstore.order.entity.OrderItem;
import com.retailstore.order.repository.OrderItemRepository;
import com.retailstore.order.repository.OrderRepository;
import com.retailstore.product.entity.Product;
import com.retailstore.testdata.OrderTestData;
import com.retailstore.testdata.ProductTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({OrderTestData.class, ProductTestData.class})
public class OrderItemRepositoryIT {

    @Autowired private OrderTestData orderTestData;
    @Autowired private ProductTestData productTestData;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;

    //================<< Create Order Item >>====================
    @Test
    @DisplayName("Save orderItem successfully")
    void saveOrderItem_success(){
        Product product = productTestData.createProduct(1L, 2000);
        Order order = orderTestData.createOrder(3L, 2000);
        OrderItem item = orderTestData.createOrderItem(order.getId(), product, 3);

        assertThat(item.getId()).isNotNull();
        assertThat(item.getOrderId()).isEqualTo(order.getId());
        assertThat(item.getProductId()).isEqualTo(product.getId());
        assertThat(item.getQuantity()).isEqualTo(3);
    }

    //================<< Find orderItem by id >>====================
    @Test
    @DisplayName("Find by id")
    void findById(){
        Product product = productTestData.createProduct(1L, 2000);
        Order order = orderTestData.createOrder(3L, 2000);
        OrderItem item = orderTestData.createOrderItem(order.getId(), product, 2);

        Optional<OrderItem> found = orderItemRepository.findById(item.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getQuantity()).isEqualTo(2);
    }

    //================<< Find orderItem by OrderId >>====================
    @Test
    @DisplayName("Find orderItem by orderItem")
    void findByIdOrderId(){
        Product product1 = productTestData.createProduct(1L, 1820);
        Product product2 = productTestData.createProduct(2L, 1516);

        Order order = orderTestData.createOrder(3L, 18980);
        orderTestData.createOrderItem(order.getId(), product1, 2);
        orderTestData.createOrderItem(order.getId(), product2, 8);

       List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

        assertThat(items).hasSize(2);
        assertThat(items).extracting(i->i.getPrice().doubleValue())
                .containsExactlyInAnyOrder(1820.0, 1516.0);
    }

    //================<< Delete orderItem >>====================
    @Test
    @DisplayName("Delete orderItem")
    void deleteOrderItem(){
        Product product = productTestData.createProduct(1L, 2000);
        Order order = orderTestData.createOrder(3L, 2000);
        OrderItem item = orderTestData.createOrderItem(order.getId(), product, 2);

        orderItemRepository.deleteById(item.getId());

        Optional<OrderItem> found = orderItemRepository.findById(item.getId());

        assertThat(found).isNotPresent();
    }
}




















