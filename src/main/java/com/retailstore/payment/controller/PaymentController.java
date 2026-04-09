package com.retailstore.payment.controller;

import com.retailstore.payment.dto.PaymentRequestDTO;
import com.retailstore.payment.dto.PaymentResponseDTO;
import com.retailstore.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller responsible for handling payment operations.
 *
 * <p>Provides endpoints for processing payments, retrieving payment
 * details, and refunding payments associated with orders.</p>
 */
@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Processes payment for an order.
     *
     * @param paymentRequestDTO payment request containing orderId and payment method
     * @return details of the processed payment
     */
    @PreAuthorize("hasRole('CUSTOMER') and @orderSecurity.isOwnerByOrderId(#paymentRequestDTO.orderId, authentication)")
    @PostMapping
    public ResponseEntity<PaymentResponseDTO> processPayment(@Valid @RequestBody PaymentRequestDTO paymentRequestDTO){

        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.processPayment(paymentRequestDTO));
    }

    /**
     * Retrieves payment details for a specific order.
     *
     * @param orderId the order identifier
     * @return payment details associated with the order
     */
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOwnerByOrderId(#orderId, authentication)")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponseDTO> getPaymentByOrderId(@PathVariable Long orderId){

        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    /**
     * Retrieves payment details using payment identifier.
     *
     * @param paymentId the payment identifier
     * @return payment details
     */
    @PreAuthorize("hasRole('ADMIN') or @paymentSecurity.isOwnerByPaymentId(#paymentId, authentication)")
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDTO> getPaymentById(@PathVariable Long paymentId){

        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }

    /**
     * Refunds payment for a given order.
     *
     * @param orderId the order identifier
     * @return refunded payment details
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{orderId}/refund")
    public ResponseEntity<PaymentResponseDTO> refundPayment(@PathVariable Long orderId){

        return ResponseEntity.ok(paymentService.refundPayment(orderId));
    }
}
