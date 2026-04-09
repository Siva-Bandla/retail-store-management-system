package com.retailstore.order.repository;

import com.retailstore.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    Optional<Order> findByIdAndDeletedFalse(Long orderId);

    boolean existsByUserIdAndDeletedFalse(Long userId);

    boolean existsByIdAndUserId(Long orderId, Long id);

    Page<Order> findByCreatedAtAfter(LocalDateTime yesterday, Pageable pageable);
}
