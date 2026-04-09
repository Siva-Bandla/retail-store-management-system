package com.retailstore.order.service;

import com.retailstore.cart.entity.Cart;
import com.retailstore.cart.entity.CartItem;
import com.retailstore.cart.repository.CartItemRepository;
import com.retailstore.cart.repository.CartRepository;
import com.retailstore.exception.ResourceConflictException;
import com.retailstore.exception.ResourceInsufficientException;
import com.retailstore.exception.ResourceNotFoundException;
import com.retailstore.inventory.entity.Inventory;
import com.retailstore.inventory.repository.InventoryRepository;
import com.retailstore.order.dto.CreateOrderRequestDTO;
import com.retailstore.order.dto.OrderItemRequestDTO;
import com.retailstore.order.dto.OrderResponseDTO;
import com.retailstore.order.dto.UpdateOrderStatusRequestDTO;
import com.retailstore.order.entity.OrderItem;
import com.retailstore.order.enums.OrderStatus;
import com.retailstore.order.entity.Order;
import com.retailstore.order.mapper.OrderMapper;
import com.retailstore.order.repository.OrderItemRepository;
import com.retailstore.order.repository.OrderRepository;
import com.retailstore.payment.service.PaymentService;
import com.retailstore.product.entity.Product;
import com.retailstore.product.repository.ProductRepository;
import com.retailstore.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service implementation responsible for handling all business operations
 * related to {@link Order} management.
 *
 * <p>This service provides functionality for:</p>
 * <ul>
 *     <li>Creating new orders</li>
 *     <li>Fetching order details</li>
 *     <li>Retrieving orders for users</li>
 *     <li>Updating order status</li>
 *     <li>Cancelling orders</li>
 * </ul>
 *
 * <p>Order creation includes validation of user existence, product availability,
 * inventory checks, and calculation of the total order amount.</p>
 */
@Service
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryRepository inventoryRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final PaymentService paymentService;

    @Autowired
    public OrderServiceImpl(UserRepository userRepository, ProductRepository productRepository,
                            OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                            InventoryRepository inventoryRepository, CartRepository cartRepository,
                            CartItemRepository cartItemRepository, PaymentService paymentService) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.inventoryRepository = inventoryRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.paymentService = paymentService;
    }


    /**
     * Creates a new order for a given user with the requested products.
     *
     * <p>This method performs the following operations:</p>
     * <ol>
     *     <li>Validates that the user exists.</li>
     *     <li>Fetches each requested product.</li>
     *     <li>Checks if sufficient inventory is available for each product.</li>
     *     <li>Deducts the requested quantity from the product inventory.</li>
     *     <li>Creates {@link OrderItem} entries for each product.</li>
     *     <li>Calculates the total order amount.</li>
     *     <li>Persists the order and its associated items.</li>
     * </ol>
     *
     * <p>The entire operation is executed within a transactional boundary to ensure
     * data consistency. If any validation fails (such as user not found, product not
     * found, or insufficient inventory), the transaction will be rolled back and
     * no changes will be committed to the database.</p>
     *
     * @param createOrderRequestDTO the request object containing the user ID and the list
     *                              of products with their requested quantities
     *
     * @return {@link OrderResponseDTO} containing details of the created order,
     *         including order items and total amount
     *
     * @throws ResourceNotFoundException if the specified user or any requested product
     *                                   does not exist in the system
     *
     * @throws ResourceInsufficientException if the available inventory quantity is
     *                                       less than the requested quantity for any product
     */
    @Override
    @Transactional
    public OrderResponseDTO createOrder(CreateOrderRequestDTO createOrderRequestDTO) {

//        log.info("Creating order for user {}", createOrderRequestDTO.getUserId());

        validateUser(createOrderRequestDTO.getUserId());

        BigDecimal totalAmount = BigDecimal.ZERO;

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequestDTO itemRequest: createOrderRequestDTO.getItems()){
            //Fetch products
            Long productId = itemRequest.getProductId();

            Product product = productRepository.findByIdAndDeletedFalse(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found or deleted with id: " + productId
                    ));

            Inventory inventory = inventoryRepository.findByProductIdAndDeletedFalse(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Inventory not found for product id: "+ productId
                    ));

            int inventoryStock = inventory.getStock();
            int requestedQty = itemRequest.getQuantity();
            //Validate stock
            if (inventoryStock < requestedQty){
                throw new ResourceInsufficientException(
                        "Insufficient stock for product id: " + productId);
            }
            //Deduct stock
            inventory.setStock(inventoryStock - requestedQty);
            inventoryRepository.save(inventory);

            //Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(requestedQty);

            orderItems.add(orderItem);

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(requestedQty));
            //Calculate total amount
            totalAmount = totalAmount.add(itemTotal);
        }

        //Create order
        Order order = new Order();
        order.setUserId(createOrderRequestDTO.getUserId());
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(totalAmount);

        //Save order
        Order savedOrder = orderRepository.save(order);

        for (OrderItem orderItem: orderItems){
            orderItem.setOrderId(savedOrder.getId());
        }

        List<OrderItem> savedOrderItems = orderItemRepository.saveAll(orderItems);

//        log.info("Order {} created successfully", savedOrder.getId());

        return OrderMapper.mapToOrderResponseDTO(savedOrder, savedOrderItems);
    }

    /**
     * Retrieves a specific order by its ID.
     *
     * @param orderId the ID of the order to fetch
     * @return {@link OrderResponseDTO} representing the order
     * @throws ResourceNotFoundException if no order is found or cancelled with the given ID
     */
    @Override
    public OrderResponseDTO getOrderById(Long orderId) {

        Order order = orderRepository.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found or cancelled with id: " + orderId
                ));

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        return OrderMapper.mapToOrderResponseDTO(order, orderItems);
    }

    /**
     * Retrieves all orders in the system.
     *
     * @return a list of {@link OrderResponseDTO} representing all orders
     */
    @Override
    public List<OrderResponseDTO> getAllOrders() {

        List<Order> orders = orderRepository.findAll();

        return orders.stream()
                .map(order -> {
                    List<OrderItem> orderItems =
                            orderItemRepository.findByOrderId(order.getId());

                    return OrderMapper.mapToOrderResponseDTO(order, orderItems);
                })
                .toList();
    }

    /**
     * Retrieves all orders placed by a specific user.
     *
     * @param userId the ID of the user whose orders are to be fetched
     * @return a list of {@link OrderResponseDTO} representing the user's orders
     * @throws ResourceNotFoundException if the user does not exist or has no orders
     */
    @Override
    public List<OrderResponseDTO> getOrdersByUser(Long userId) {

        validateUser(userId);

        List<Order> orders = orderRepository.findByUserId(userId);

        if (orders.isEmpty()) throw new ResourceNotFoundException("no orders found for user id: " + userId);

        return orders.stream()
                .map(order -> {
                    List<OrderItem> items =
                            orderItemRepository.findByOrderId(order.getId());
                    return OrderMapper.mapToOrderResponseDTO(order, items);
                }).toList();
    }

    /**
     * Updates the status of an existing order.
     *
     * @param orderId the ID of the order to update
     * @param request  the new status to set (must match {@link OrderStatus})
     * @return {@link OrderResponseDTO} representing the updated order
     * @throws ResourceNotFoundException   if the order with the given ID does not exist or is deleted
     * @throws IllegalArgumentException    if the provided status is invalid
     */
    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatus(Long orderId, UpdateOrderStatusRequestDTO request) {

        Order order = orderRepository.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId
                ));

        if (request.getStatus().equals(OrderStatus.PAID)){
            throw new IllegalArgumentException("Process payment to set status as " + OrderStatus.PAID);
        }

        order.setStatus(request.getStatus());

        Order updatedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(updatedOrder.getId());

        return OrderMapper.mapToOrderResponseDTO(updatedOrder, orderItems);
    }

    /**
     * Cancels an existing order using soft delete.
     *
     * @param orderId the ID of the order to cancel
     * @return {@link OrderResponseDTO} representing the canceled order.
     * @throws ResourceNotFoundException if the order with the given ID does not exist or is already deleted
     */
    @Override
    @Transactional
    public OrderResponseDTO cancelOrder(Long orderId) {

        Order order = orderRepository.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId
                ));

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        if (orderItems.isEmpty()){
            throw new ResourceInsufficientException("No order items to cancel: " + orderId);
        }

        List<Long> productIds = orderItems.stream()
                .map(OrderItem::getProductId)
                .toList();

        Map<Long, Inventory> inventoryMap = inventoryRepository.findByProductIdInAndDeletedFalse(productIds)
                .stream()
                .collect(Collectors.toMap(Inventory::getProductId, i -> i));

        //Restore inventory for each item
        for (OrderItem item: orderItems){

            Inventory inventory = inventoryMap.get(item.getProductId());
            if (inventory == null){
                throw new ResourceNotFoundException(
                        "Inventory not found for product id: " + item.getProductId());
            }

            //Auto save inventory
            inventory.setStock(inventory.getStock() + item.getQuantity());
        }

        //refund if already paid
        if(order.getStatus() == OrderStatus.PAID){
            paymentService.refundPayment(orderId);
        }

        order.setDeleted(true);
        order.setStatus(OrderStatus.CANCELLED);
        Order canceledOrder = orderRepository.save(order);

        return OrderMapper.mapToOrderResponseDTO(canceledOrder, orderItems);
    }

    /**
     * Performs checkout by converting the user's cart items into an order.
     *
     * <p>This method retrieves the user's cart, validates product availability
     * and inventory stock, deducts the requested quantities, creates
     * {@link OrderItem} entries, calculates the total amount, and persists
     * the {@link Order}. After successful order creation, all items in the
     * cart are cleared.</p>
     *
     * <p>The operation runs within a transactional boundary to ensure data
     * consistency. If any validation fails, the transaction is rolled back.</p>
     *
     * @param userId the ID of the user performing checkout
     * @return {@link OrderResponseDTO} containing the created order details
     *
     * @throws ResourceNotFoundException if the user, cart, product, or inventory is not found
     * @throws ResourceConflictException if the cart is empty
     * @throws ResourceInsufficientException if requested quantity exceeds available stock
     */
    @Override
    @Transactional
    public OrderResponseDTO createOrderFromCart(Long userId) {

        validateUser(userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        if (cartItems.isEmpty()){
            throw new ResourceConflictException("Cart is empty");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem: cartItems){

            Product product = productRepository.findByIdAndDeletedFalse(cartItem.getProductId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Product not found: " + cartItem.getProductId()));

            Inventory inventory = inventoryRepository.findByProductIdAndDeletedFalse(cartItem.getProductId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Inventory not found for product: " + cartItem.getProductId()));

            int requestedQty = cartItem.getQuantity();
            if (inventory.getStock() < requestedQty){
                throw new ResourceInsufficientException("Insufficient stock for product: " + cartItem.getProductId());
            }

            inventory.setStock(inventory.getStock() - requestedQty);
            inventoryRepository.save(inventory);

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(requestedQty);

            orderItems.add(orderItem);

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(requestedQty));
            totalAmount = totalAmount.add(itemTotal);
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        for (OrderItem item: orderItems){
            item.setOrderId(savedOrder.getId());
        }

        List<OrderItem> savedOrderItems = orderItemRepository.saveAll(orderItems);

        //clear cart
        cartItemRepository.deleteAll(cartItems);

        return OrderMapper.mapToOrderResponseDTO(savedOrder, savedOrderItems);
    }

    /**
     * Validates that a user exists.
     */
    private void validateUser(Long userId) {
        if (!userRepository.existsById(userId)){
            throw new ResourceNotFoundException(
                    "User does not exist with id: " + userId + " Please register to place order");
        }
    }
}
