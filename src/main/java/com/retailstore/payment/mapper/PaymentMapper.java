package com.retailstore.payment.mapper;

import com.retailstore.payment.dto.PaymentResponseDTO;
import com.retailstore.payment.entity.Payment;

public class PaymentMapper {

    private PaymentMapper(){}

    public static PaymentResponseDTO mapToPaymentResponseDTO(Payment payment){

        return PaymentResponseDTO.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .transactionId(payment.getTransactionId())
                .build();
    }
}
