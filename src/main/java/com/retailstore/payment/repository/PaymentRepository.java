package com.retailstore.payment.repository;

import com.retailstore.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    @Query(value = """
            SELECT COUNT(*) > 0
            FROM payments p
            JOIN orders o ON p.order_id = o.order_id
            WHERE p.payment_id = :paymentId
              AND o.user_id = :userId
            """, nativeQuery = true)
    boolean existsByIdAndOrderUserId(Long paymentId, Long userId);
}
