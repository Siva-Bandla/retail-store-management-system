package com.retailstore.payment.service;

import com.retailstore.exception.ResourceConflictException;
import com.retailstore.exception.ResourceNotFoundException;
import com.retailstore.order.entity.Order;
import com.retailstore.order.enums.OrderStatus;
import com.retailstore.order.repository.OrderRepository;
import com.retailstore.payment.dto.PaymentRequestDTO;
import com.retailstore.payment.dto.PaymentResponseDTO;
import com.retailstore.payment.entity.Payment;
import com.retailstore.payment.enums.PaymentStatus;
import com.retailstore.payment.mapper.PaymentMapper;
import com.retailstore.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentServiceImpl(OrderRepository orderRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Processes payment for a given order.
     *
     * <p>This method validates the order, ensures that payment has not already
     * been completed, creates a payment record, and updates the order status
     * upon successful payment.</p>
     *
     * @param paymentRequestDTO payment request containing orderId and payment method
     * @return payment details
     *
     * @throws ResourceNotFoundException if the order does not exist
     * @throws ResourceConflictException if the order is already paid
     */
    @Override
    @Transactional
    public PaymentResponseDTO processPayment(PaymentRequestDTO paymentRequestDTO) {

        Long orderId = paymentRequestDTO.getOrderId();

        Order order = orderRepository.findByIdAndDeletedFalse(orderId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Order not found with id: " + orderId));

        paymentRepository.findByOrderId(orderId)
                .ifPresent(p -> {
                    if (p.getPaymentStatus() == PaymentStatus.SUCCESS){
                        throw new ResourceConflictException(
                                "Payment already completed for order: " + orderId);
                    }
                });

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(paymentRequestDTO.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        //mock transaction id
        payment.setTransactionId("TXN-" + System.currentTimeMillis());
        Payment savedPayment = paymentRepository.save(payment);

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        return PaymentMapper.mapToPaymentResponseDTO(savedPayment);
    }

    /**
     * Retrieves payment details for a specific order.
     *
     * @param orderId order identifier
     * @return payment details
     *
     * @throws ResourceNotFoundException if payment does not exist
     */
    @Override
    public PaymentResponseDTO getPaymentByOrderId(Long orderId) {

        if (!orderRepository.existsById(orderId)){
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));

        return PaymentMapper.mapToPaymentResponseDTO(payment);
    }

    /**
     * Retrieves payment details by payment id.
     *
     * @param paymentId payment identifier
     * @return payment details
     *
     * @throws ResourceNotFoundException if payment does not exist
     */
    @Override
    public PaymentResponseDTO getPaymentById(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + paymentId));

        return PaymentMapper.mapToPaymentResponseDTO(payment);
    }

    /**
     * Refunds payment for a given order.
     *
     * <p>This method verifies that a successful payment exists for the order
     * and marks the payment as refunded. It also updates the order status
     * accordingly.</p>
     *
     * @param orderId the order identifier
     * @return refunded payment details
     *
     * @throws ResourceNotFoundException if payment or order is not found
     * @throws ResourceConflictException if payment is already refunded
     */
    @Override
    @Transactional
    public PaymentResponseDTO refundPayment(Long orderId) {

        if (!orderRepository.existsById(orderId)){
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));

        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED){
            throw new ResourceConflictException("Payment already refunded for order: " + orderId);
        }

        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS){
            throw new ResourceConflictException("Cannot refund unsuccessful payment for order: " + orderId);
        }

        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        Payment refundedPayment = paymentRepository.save(payment);

        return PaymentMapper.mapToPaymentResponseDTO(refundedPayment);
    }
}
