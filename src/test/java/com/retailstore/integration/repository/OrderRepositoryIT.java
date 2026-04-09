package com.retailstore.integration.repository;

import com.retailstore.order.entity.Order;
import com.retailstore.order.enums.OrderStatus;
import com.retailstore.order.repository.OrderRepository;
import com.retailstore.testdata.OrderTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(OrderTestData.class)
public class OrderRepositoryIT {

    @Autowired private OrderTestData orderTestData;
    @Autowired private OrderRepository orderRepository;

    //================<< Create Order >>====================
    @Test
    @DisplayName("Save order successfully")
    void saveOrder_success(){
        Order order = orderTestData.createOrder(1L, 150.50);

        assertThat(order.getId()).isNotNull();
        assertThat(order.getUserId()).isEqualTo(1L);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(150.50));
    }

    //================<< Find order by userId >>====================
    @Test
    @DisplayName("Find by userId returns all orders of the user")
    void findByUserId_returnsUserOrders(){
        orderTestData.createOrder(10L, 100);
        orderTestData.createOrder(10L, 200);
        orderTestData.createOrder(20L, 300);

        List<Order> orders = orderRepository.findByUserId(10L);

        assertThat(orders).hasSize(2);
        assertThat(orders).allMatch(o->o.getUserId().equals(10L));
    }

    @Test
    @DisplayName("Find by userId returns empty list")
    void findByUserId_returnsEmptyList(){
        List<Order> orders = orderRepository.findByUserId(10L);

        assertThat(orders).isEmpty();
    }

    //================<< Find order by id and deleted false >>====================
    @Test
    @DisplayName("Find order by id and deleted false")
    void findByIdAndDeletedFalse_notDeleted(){
        Order order = orderTestData.createOrder(1L, 120);

        Optional<Order> found = orderRepository.findByIdAndDeletedFalse(order.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getDeleted()).isFalse();
    }

    @Test
    @DisplayName("Find order by id and deleted false return empty for deleted order")
    void findByIdAndDeletedFalse_deleted(){
        Order order = orderTestData.createOrder(1L, 150);
        order.setDeleted(true);
        orderRepository.save(order);

        Optional<Order> found = orderRepository.findByIdAndDeletedFalse(order.getId());

        assertThat(found).isNotPresent();
    }

    //================<< Exists by userId and deleted false >>====================
    @Test
    @DisplayName("Exists by userId and deleted false")
    void existsByUserIdAndDeletedFalse_true(){
        Order order = orderTestData.createOrder(1L, 123);

        boolean exists = orderRepository.existsByUserIdAndDeletedFalse(order.getUserId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Exists by userId and deleted false and return false for deleted orders")
    void existsByUserIdAndDeletedFalse_false(){
        Order order = orderTestData.createOrder(1L, 123);
        order.setDeleted(true);
        orderRepository.save(order);

        boolean exists = orderRepository.existsByUserIdAndDeletedFalse(order.getUserId());

        assertThat(exists).isFalse();
    }

    //================<< Exists by id and userId >>====================
    @Test
    @DisplayName("Exists by id and userId")
    void existsByIdAndUserId_true(){
        Order order = orderTestData.createOrder(1L, 345);

        boolean exists = orderRepository.existsByIdAndUserId(order.getId(), order.getUserId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Exists by id and userId and return false when user mismatches")
    void existsByIdAndUserId_false(){
        Order order = orderTestData.createOrder(1L, 533);

        boolean exists = orderRepository.existsByUserIdAndDeletedFalse(999L);

        assertThat(exists).isFalse();
    }

    //================<< BaseEntity: createdAt >>====================
    @Test
    @DisplayName("createdAt is auto-populated on save")
    void createdAt_isSet(){
        Order order = orderTestData.createOrder(5L, 390);

        assertThat(order.getCreatedAt()).isNotNull();
        assertThat(order.getUpdatedAt()).isNull();
    }

    //================<< BaseEntity: updatedAt >>====================
    @Test
    @DisplayName("updatedAt when updated")
    void updatedAt_isUpdated() throws InterruptedException {
        Order order = orderTestData.createOrder(5L, 390);

        LocalDateTime createdTime = order.getCreatedAt();
        assertThat(order.getUpdatedAt()).isNull();

        Thread.sleep(10);

        order.setTotalAmount(BigDecimal.valueOf(200));
        Order updated = orderRepository.saveAndFlush(order);

        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(updated.getUpdatedAt()).isAfter(createdTime);
    }
}




















