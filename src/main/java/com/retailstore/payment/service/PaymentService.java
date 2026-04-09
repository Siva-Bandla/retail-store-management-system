package com.retailstore.payment.service;

import com.retailstore.payment.dto.PaymentRequestDTO;
import com.retailstore.payment.dto.PaymentResponseDTO;

public interface PaymentService {

    PaymentResponseDTO processPayment(PaymentRequestDTO paymentRequestDTO);
    PaymentResponseDTO getPaymentByOrderId(Long orderId);
    PaymentResponseDTO getPaymentById(Long paymentId);
    PaymentResponseDTO refundPayment(Long orderId);
}
