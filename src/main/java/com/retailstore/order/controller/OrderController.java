package com.retailstore.order.controller;

import com.retailstore.order.dto.CreateOrderRequestDTO;
import com.retailstore.order.dto.OrderResponseDTO;
import com.retailstore.order.dto.UpdateOrderStatusRequestDTO;
import com.retailstore.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing orders.
 * Supports creating, retrieving, updating status, and soft-deleting orders.
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Creates a new order.
     *
     * @param orderRequest DTO containing order details
     * @return {@link OrderResponseDTO} of the created order
     */
    @PreAuthorize("#orderRequest.userId == authentication.principal.id")
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody CreateOrderRequestDTO orderRequest){

        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(orderRequest));
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param orderId ID of the order to retrieve
     * @return {@link OrderResponseDTO} of the order
     */
    @PreAuthorize("@orderSecurity.isOwnerByOrderId(#orderId, authentication)")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long orderId){

        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    /**
     * Retrieves all orders.
     *
     * @return List of {@link OrderResponseDTO} for all orders
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders(){

        return ResponseEntity.ok(orderService.getAllOrders());
    }

    /**
     * Retrieves all orders.
     *
     * @return List of {@link OrderResponseDTO} for all orders
     */
    @PreAuthorize("#userId == authentication.principal.id")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByUser(@PathVariable Long userId){

        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    /**
     * Updates the status of an existing order.
     *
     * @param orderId ID of the order to update
     * @param status  New status for the order (must match {@link com.retailstore.order.enums.OrderStatus})
     * @return {@link OrderResponseDTO} of the updated order
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(@PathVariable Long orderId,
                                                              @Valid @RequestBody UpdateOrderStatusRequestDTO status){

        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

    /**
     * Cancels an existing order using soft delete.
     *
     * @param orderId ID of the order to cancel
     * @return {@link OrderResponseDTO} of the canceled order
     */
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOwnerByOrderId(#orderId, authentication)")
    @DeleteMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Long orderId){

        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }

    /**
     * Creates an order from cart.
     *
     * @param userId ID of the user to check out
     * @return {@link OrderResponseDTO} of the created order
     */
    @PreAuthorize("#userId == authentication.principal.id")
    @PostMapping("/checkout/{userId}")
    public ResponseEntity<OrderResponseDTO> checkout(@PathVariable Long userId){

        return ResponseEntity.ok(orderService.createOrderFromCart(userId));
    }
}
