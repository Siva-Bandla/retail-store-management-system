package com.retailstore.integration.repository;

import com.retailstore.order.entity.Order;
import com.retailstore.payment.entity.Payment;
import com.retailstore.payment.repository.PaymentRepository;
import com.retailstore.testdata.OrderTestData;
import com.retailstore.testdata.PaymentTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({OrderTestData.class, PaymentTestData.class})
public class PaymentRepositoryIT {

    @Autowired private PaymentTestData paymentTestData;
    @Autowired private OrderTestData orderTestData;
    @Autowired private PaymentRepository paymentRepository;

    //================<< Process Payment >>====================
    @Test
    @DisplayName("Process payment successfully")
    void processPayment_success(){
        Payment payment = paymentTestData.createPayment(12L, 150.50);

        assertThat(payment.getId()).isNotNull();
        assertThat(payment.getOrderId()).isEqualTo(12L);
    }

    //================<< Find payment by orderId >>====================
    @Test
    @DisplayName("Find by orderId")
    void findByOrderId(){
        Payment payment = paymentTestData.createPayment(12L, 150.50);

        Optional<Payment> found = paymentRepository.findByOrderId(payment.getOrderId());

        assertThat(found).isPresent();
        assertThat(found.get().getAmount()).isEqualByComparingTo("150.50");
    }

    @Test
    @DisplayName("Find order by id and deleted false return empty for deleted order")
    void findByOrderId_shouldReturnEmpty(){
        Optional<Payment> found = paymentRepository.findByOrderId(999L);

        assertThat(found).isEmpty();
    }

    //================<< Exists by id and orderUserId >>====================
    @Test
    @DisplayName("Exists by id and orderUserId")
    void existsByIdAndOrderUserId_true(){
        Order order = orderTestData.createOrder(13L, 234);
        Payment payment = paymentTestData.createPayment(order.getId(), 234);

        boolean exists = paymentRepository.existsByIdAndOrderUserId(payment.getId(), 13L);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Exists by id and orderUserId and return false")
    void existsByIdAndOrderUserId_false(){
        Order order = orderTestData.createOrder(13L, 234);
        Payment payment = paymentTestData.createPayment(order.getId(), 234);

        boolean exists = paymentRepository.existsByIdAndOrderUserId(payment.getId(), 15L);

        assertThat(exists).isFalse();
    }

    //================<< Find payment by id >>====================
    @Test
    @DisplayName("Find payment by Id")
    void findById(){
        Payment payment = paymentTestData.createPayment(12L, 150.50);

        Optional<Payment> found = paymentRepository.findById(payment.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getAmount()).isEqualByComparingTo("150.50");
    }

    @Test
    @DisplayName("Find order by id and deleted false return empty for deleted order")
    void findById_false(){
        Optional<Payment> found = paymentRepository.findById(999L);

        assertThat(found).isEmpty();
    }
}
