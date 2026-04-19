package com.retailstore.payment.service;

import com.retailstore.payment.dto.PaymentRequestDTO;
import com.retailstore.payment.dto.PaymentResponseDTO;
import com.retailstore.payment.enums.PaymentMethod;

import java.util.List;

public interface PaymentService {

    PaymentResponseDTO processPayment(PaymentRequestDTO paymentRequestDTO);
    PaymentResponseDTO getPaymentByOrderId(Long orderId);
    PaymentResponseDTO getPaymentById(Long paymentId);
    PaymentResponseDTO refundPayment(Long orderId);

    List<PaymentMethod> getPaymentMethods();
}
