package com.retailstore.testdata;

import com.retailstore.payment.entity.Payment;
import com.retailstore.payment.enums.PaymentMethod;
import com.retailstore.payment.enums.PaymentStatus;
import com.retailstore.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

public class PaymentTestData {

    @Autowired
    private PaymentRepository paymentRepository;

    public Payment createPayment(Long orderId, double amount) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(BigDecimal.valueOf(amount));
        payment.setPaymentMethod(PaymentMethod.CARD);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId("txn_" + System.nanoTime());

        return paymentRepository.save(payment);
    }
}