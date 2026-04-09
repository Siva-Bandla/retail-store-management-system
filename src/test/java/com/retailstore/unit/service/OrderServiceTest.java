package com.retailstore.unit.service;

import com.retailstore.cart.entity.Cart;
import com.retailstore.cart.entity.CartItem;
import com.retailstore.cart.repository.CartItemRepository;
import com.retailstore.cart.repository.CartRepository;
import com.retailstore.exception.ResourceInsufficientException;
import com.retailstore.exception.ResourceNotFoundException;
import com.retailstore.inventory.entity.Inventory;
import com.retailstore.inventory.repository.InventoryRepository;
import com.retailstore.order.dto.CreateOrderRequestDTO;
import com.retailstore.order.dto.OrderItemRequestDTO;
import com.retailstore.order.dto.OrderResponseDTO;
import com.retailstore.order.dto.UpdateOrderStatusRequestDTO;
import com.retailstore.order.entity.Order;
import com.retailstore.order.entity.OrderItem;
import com.retailstore.order.enums.OrderStatus;
import com.retailstore.order.repository.OrderItemRepository;
import com.retailstore.order.repository.OrderRepository;
import com.retailstore.order.service.OrderServiceImpl;
import com.retailstore.payment.service.PaymentService;
import com.retailstore.product.entity.Product;
import com.retailstore.product.repository.ProductRepository;
import com.retailstore.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private PaymentService paymentService;

    @InjectMocks
    private OrderServiceImpl orderService;

    //================<< BUILDERS >>================
    private CreateOrderRequestDTO buildOrderRequest(){
        OrderItemRequestDTO itemRequest = new OrderItemRequestDTO();
        itemRequest.setProductId(10L);
        itemRequest.setQuantity(2);

        CreateOrderRequestDTO orderRequest = new CreateOrderRequestDTO();
        orderRequest.setUserId(1L);
        orderRequest.setItems(List.of(itemRequest));

        return orderRequest;
    }

    private Product buildProduct(){
        Product product = new Product();
        product.setId(10L);
        product.setPrice(BigDecimal.valueOf(100));
        product.setName("Test Product");

        return product;
    }

    private Inventory buildInventory(int stock){
        Inventory inventory = new Inventory();
        inventory.setProductId(10L);
        inventory.setStock(stock);

        return inventory;
    }

    private Order buildOrder(Long id, OrderStatus status){
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);

        return order;
    }

    private OrderItem buildOrderItem(){
        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(10L);
        orderItem.setQuantity(2);

        return orderItem;
    }

    private void mockOrderSave(){
        when(orderRepository.save(any())).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            return order;
        });

        when(orderItemRepository.saveAll(any())).thenReturn(List.of(new OrderItem()));
    }


    //================<< CREATE ORDER >>================
    @Nested
    class CreateOrderTests{

        @Test
        void shouldCreateOrderSuccessfully(){
            when(userRepository.existsById(1L)).thenReturn(true);
            when(productRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(buildProduct()));
            when(inventoryRepository.findByProductIdAndDeletedFalse(10L)).thenReturn(Optional.of(buildInventory(10)));

            mockOrderSave();

            OrderResponseDTO response = orderService.createOrder(buildOrderRequest());

            assertNotNull(response);
            assertEquals(OrderStatus.CREATED, response.getStatus());

            verify(inventoryRepository).save(any());
            verify(orderRepository).save(any());
        }

        @Test
        void shouldThrowException_whenUserNotFound(){
            when(userRepository.existsById(1L)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class,
                    () -> orderService.createOrder(buildOrderRequest()));
        }

        @Test
        void shouldThrowException_whenProductNotFound(){
            when(userRepository.existsById(1L)).thenReturn(true);
            when(productRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> orderService.createOrder(buildOrderRequest()));
        }

        @Test
        void shouldThrowException_whenInventoryNotFound(){
            when(userRepository.existsById(1L)).thenReturn(true);
            when(productRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(buildProduct()));
            when(inventoryRepository.findByProductIdAndDeletedFalse(10L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> orderService.createOrder(buildOrderRequest()));
        }

        @Test void shouldThrowException_whenInsufficientStock(){
            when(userRepository.existsById(1L)).thenReturn(true);
            when(productRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(buildProduct()));
            when(inventoryRepository.findByProductIdAndDeletedFalse(10L)).thenReturn(Optional.of(buildInventory(1)));

            assertThrows(ResourceInsufficientException.class,
                    () -> orderService.createOrder((buildOrderRequest())));
        }
    }


    //================<< GET ORDER By ID >>=================
    @Nested
    class GetOrderByIdTests{

        @Test
        void shouldGetOrderByIdSuccessfully(){
            when(orderRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(buildOrder(1L, OrderStatus.CREATED)));
            when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(buildOrderItem()));

            OrderResponseDTO response = orderService.getOrderById(1L);

            assertNotNull(response);
            assertEquals(OrderStatus.CREATED, response.getStatus());
        }

        @Test
        void shouldThrowException_whenOrderNotFound(){
            when(orderRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> orderService.getOrderById(1L));
        }
    }

    //================<< GET ALL ORDERS >>=================
    @Nested
    class GetAllOrdersTests{

        @Test
        void shouldGetAllOrdersSuccessfully(){
            when(orderRepository.findAll()).thenReturn(List.of(buildOrder(1L, OrderStatus.CREATED)));
            when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(buildOrderItem()));

            List<OrderResponseDTO> result = orderService.getAllOrders();

            assertNotNull(result);
            assertEquals(1, result.size());

            verify(orderRepository).findAll();
            verify(orderItemRepository).findByOrderId(1L);
        }

        @Test
        void shouldReturnEmptyList_whenNoOrderExist(){

            when(orderRepository.findAll()).thenReturn(List.of());

            List<OrderResponseDTO> result = orderService.getAllOrders();

            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(orderItemRepository, never()).findByOrderId(any());
        }

        @Test
        void shouldReturnOrderWithEmptyItems(){
            when(orderRepository.findAll()).thenReturn(List.of(buildOrder(1L, OrderStatus.CREATED)));
            when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of());

            List<OrderResponseDTO> result = orderService.getAllOrders();

            assertEquals(1, result.size());

            verify(orderItemRepository).findByOrderId(1L);
        }

        @Test
        void shouldReturnMultipleOrders(){
            when(orderRepository.findAll()).thenReturn(
                    List.of(buildOrder(1L, OrderStatus.CREATED), buildOrder(2L, OrderStatus.CREATED)));
            when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(buildOrderItem()));
            when(orderItemRepository.findByOrderId(2L)).thenReturn(List.of(buildOrderItem()));

            List<OrderResponseDTO> result = orderService.getAllOrders();

            assertNotNull(result);
            assertEquals(2, result.size());

            verify(orderItemRepository).findByOrderId(1L);
            verify(orderItemRepository).findByOrderId(2L);
        }
    }

    //================<< GET ORDERS BY USER >>=================
    @Nested
    class GetOrderByUserTests{

        @Test
        void shouldGetOrdersByUserSuccessfully(){
            when(userRepository.existsById(1L)).thenReturn(true);
            when(orderRepository.findByUserId(1L)).thenReturn(List.of(buildOrder(1L, OrderStatus.CREATED)));
            when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(buildOrderItem()));

            List<OrderResponseDTO> result = orderService.getOrdersByUser(1L);

            assertFalse(result.isEmpty());
        }

        @Test
        void shouldThrowException_whenUserNotExists_forGetOrders(){
            when(userRepository.existsById(1L)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class,
                    () -> orderService.getOrdersByUser(1L));
        }

        @Test
        void shouldThrowException_whenNoOrdersFound(){
            when(userRepository.existsById(1L)).thenReturn(true);
            when(orderRepository.findByUserId(1L)).thenReturn(List.of());

            assertThrows(ResourceNotFoundException.class,
                    () -> orderService.getOrdersByUser(1L));
        }
    }

    //================<< UPDATE STATUS >>=================
    @Nested
    class UpdateOrderStatusTests{

        @Test
        void shouldUpdateOrderStatus(){
            UpdateOrderStatusRequestDTO updateRequest = new UpdateOrderStatusRequestDTO();
            updateRequest.setStatus(OrderStatus.SHIPPED);

            when(orderRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(buildOrder(10L, OrderStatus.CREATED)));
            when(orderRepository.save(any())).thenAnswer(
                    invocation -> invocation.getArgument(0));
            when(orderItemRepository.findByOrderId(10L)).thenReturn(List.of(buildOrderItem()));

            OrderResponseDTO response = orderService.updateOrderStatus(10L, updateRequest);

            assertNotNull(response);
            assertEquals(OrderStatus.SHIPPED, response.getStatus());
        }

        @Test
        void shouldThrowException_whenSettingPaidDirectly(){
            UpdateOrderStatusRequestDTO request = new UpdateOrderStatusRequestDTO();
            request.setStatus(OrderStatus.PAID);

            when(orderRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(buildOrder(10L, OrderStatus.CREATED)));

            assertThrows(IllegalArgumentException.class,
                    () -> orderService.updateOrderStatus(1L, request));
        }
    }

    //================<< CANCEL ORDER >>=================
    @Nested
    class CancelOrderTests{

        @Test
        void shouldCancelOrderSuccessfully(){
            when(orderRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(buildOrder(10L, OrderStatus.CREATED)));
            when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(buildOrderItem()));
            when(inventoryRepository.findByProductIdInAndDeletedFalse(any())).thenReturn(List.of(buildInventory(5)));
            when(orderRepository.save(any())).thenAnswer(
                    invocation -> invocation.getArgument(0));

            OrderResponseDTO response = orderService.cancelOrder(1L);

            assertEquals(OrderStatus.CANCELLED, response.getStatus());
            verify(inventoryRepository).findByProductIdInAndDeletedFalse(any());
        }

        @Test
        void shouldRefund_whenPaid(){
            when(orderRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(buildOrder(10L, OrderStatus.PAID)));
            when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(buildOrderItem()));
            when(inventoryRepository.findByProductIdInAndDeletedFalse(any())).thenReturn(List.of(buildInventory(5)));
            when(orderRepository.save(any())).thenAnswer(
                    invocation -> invocation.getArgument(0));

            OrderResponseDTO response = orderService.cancelOrder(1L);

            assertEquals(OrderStatus.CANCELLED, response.getStatus());

            verify(paymentService).refundPayment(1L);
        }
    }

    //================<< CREATE ORDER FROM CART >>=================
    @Nested
    class CreateOrderFromCartTests{

        @Test
        void shouldCreateOrderFromCartSuccessfully(){

            when(userRepository.existsById(1L)).thenReturn(true);

            Cart cart = new Cart();
            cart.setId(1L);

            CartItem cartItem = new CartItem();
            cartItem.setProductId(10L);
            cartItem.setQuantity(2);

            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(cartItem));
            when(productRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(buildProduct()));
            when(inventoryRepository.findByProductIdAndDeletedFalse(10L)).thenReturn(Optional.of(buildInventory(10)));

            mockOrderSave();

            OrderResponseDTO response = orderService.createOrderFromCart(1L);

            assertNotNull(response);
            assertEquals(OrderStatus.CREATED, response.getStatus());

            verify(cartItemRepository).deleteAll(any());
        }
    }
}