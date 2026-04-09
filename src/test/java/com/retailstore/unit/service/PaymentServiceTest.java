package com.retailstore.unit.service;

import com.retailstore.exception.ResourceConflictException;
import com.retailstore.exception.ResourceNotFoundException;
import com.retailstore.order.entity.Order;
import com.retailstore.order.enums.OrderStatus;
import com.retailstore.order.repository.OrderRepository;
import com.retailstore.payment.dto.PaymentRequestDTO;
import com.retailstore.payment.dto.PaymentResponseDTO;
import com.retailstore.payment.entity.Payment;
import com.retailstore.payment.enums.PaymentMethod;
import com.retailstore.payment.enums.PaymentStatus;
import com.retailstore.payment.repository.PaymentRepository;
import com.retailstore.payment.service.PaymentServiceImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    //================<< BUILDERS >>================
    private PaymentRequestDTO buildPaymentRequest(Long orderId){
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setOrderId(orderId);
        request.setPaymentMethod(PaymentMethod.CARD);

        return request;
    }

    private Order buildOrder(Long id, OrderStatus status){
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        order.setTotalAmount(BigDecimal.valueOf(200));

        return order;
    }

    private Payment buildPayment(Long orderId, PaymentStatus paymentStatus){
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setOrderId(orderId);
        payment.setPaymentStatus(paymentStatus);
        payment.setAmount(BigDecimal.valueOf(200));

        return payment;
    }

    //===============<< PROCESS PAYMENT >>================
    @Nested
    class ProcessPaymentTests{

        @Test
        void shouldProcessPaymentSuccessfully(){
            when(orderRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(Optional.of(buildOrder(1L, OrderStatus.CREATED)));
            when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
            when(paymentRepository.save(any()))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(orderRepository.save(any()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            PaymentResponseDTO response = paymentService.processPayment(buildPaymentRequest(1L));

            assertNotNull(response);
            assertEquals(PaymentStatus.SUCCESS, response.getPaymentStatus());
            assertEquals(PaymentMethod.CARD, response.getPaymentMethod());

            verify(orderRepository).save(argThat(order -> order.getStatus() == OrderStatus.PAID));
            verify(paymentRepository).save(any());
        }

        @Test
        void shouldThrowException_whenOrderNotFound(){
            when(orderRepository.findByIdAndDeletedFalse(2L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> paymentService.processPayment(buildPaymentRequest(2L)));
        }

        @Test
        void shouldThrowException_whenPaymentAlreadySuccess(){
            when(orderRepository.findByIdAndDeletedFalse(3L))
                    .thenReturn(Optional.of(buildOrder(3L, OrderStatus.PAID)));
            when(paymentRepository.findByOrderId(3L))
                    .thenReturn(Optional.of(buildPayment(3L, PaymentStatus.SUCCESS)));

            assertThrows(ResourceConflictException.class,
                    () -> paymentService.processPayment(buildPaymentRequest(3L)));
        }
    }

    //===============<< GET PAYMENT BY ORDER ID >>================
    @Nested
    class GetPaymentByOrderIdTests{

        @Test
        void getPaymentByOrderIdSuccessfully(){
            when(orderRepository.existsById(4L)).thenReturn(true);
            when(paymentRepository.findByOrderId(4L))
                    .thenReturn(Optional.of(buildPayment(4L, PaymentStatus.FAILED)));

            PaymentResponseDTO response = paymentService.getPaymentByOrderId(4L);

            assertNotNull(response);
            assertEquals(PaymentStatus.FAILED, response.getPaymentStatus());
        }

        @Test
        void shouldThrowException_whenOrderNotFound(){
            when(orderRepository.existsById(5L)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class,
                    () -> paymentService.getPaymentByOrderId(5L));
        }

        @Test
        void shouldThrowException_whenPaymentNotFound(){
            when(orderRepository.existsById(6L)).thenReturn(true);
            when(paymentRepository.findByOrderId(6L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> paymentService.getPaymentByOrderId(6L));
        }
    }

    //===============<< GET PAYMENT BY ID >>================
    @Nested
    class GetPaymentByIdTests{

        @Test
        void shouldGetPaymentByIdSuccessfully(){
            when(paymentRepository.findById(7L))
                    .thenReturn(Optional.of(buildPayment(7L, PaymentStatus.PENDING)));

            PaymentResponseDTO response = paymentService.getPaymentById(7L);

            assertNotNull(response);
            assertEquals(PaymentStatus.PENDING, response.getPaymentStatus());
        }

        @Test
        void shouldThrowException_whenPaymentNotFound(){
            when(paymentRepository.findById(8L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> paymentService.getPaymentById(8L));
        }
    }

    //===============<< REFUND PAYMENT >>================
    @Nested
    class RefundPaymentTests{

        @Test
        void shouldRefundPaymentSuccessfully(){
            when(orderRepository.existsById(9L)).thenReturn(true);
            when(paymentRepository.findByOrderId(9L))
                    .thenReturn(Optional.of(buildPayment(9L, PaymentStatus.SUCCESS)));
            when(paymentRepository.save(any()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            PaymentResponseDTO response = paymentService.refundPayment(9L);

            assertNotNull(response);
            assertEquals(PaymentStatus.REFUNDED, response.getPaymentStatus());

            verify(paymentRepository).save(any());
        }

        @Test
        void shouldThrowException_whenOrderNotFound(){
            when(orderRepository.existsById(10L)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class,
                    () -> paymentService.refundPayment(10L));
        }

        @Test
        void shouldThrowException_whenPaymentNotFound(){
            when(orderRepository.existsById(11L)).thenReturn(true);
            when(paymentRepository.findByOrderId(11L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> paymentService.refundPayment(11L));
        }

        @Test
        void shouldThrowException_whenPaymentAlreadyRefunded(){
            when(orderRepository.existsById(11L)).thenReturn(true);
            when(paymentRepository.findByOrderId(11L))
                    .thenReturn(Optional.of(buildPayment(11L, PaymentStatus.REFUNDED)));

            assertThrows(ResourceConflictException.class,
                    () -> paymentService.refundPayment(11L));
        }

        @Test
        void shouldThrowException_whenPaymentNotSuccess(){
            when(orderRepository.existsById(12L)).thenReturn(true);
            when(paymentRepository.findByOrderId(12L))
                    .thenReturn(Optional.of(buildPayment(12L, PaymentStatus.PENDING)));

            assertThrows(ResourceConflictException.class,
                    () -> paymentService.refundPayment(12L));
        }
    }
}
