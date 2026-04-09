package com.retailstore.cart.service;

import com.retailstore.cart.dto.AddToCartRequestDTO;
import com.retailstore.cart.dto.CartItemDTO;
import com.retailstore.cart.dto.CartResponseDTO;
import com.retailstore.cart.entity.Cart;
import com.retailstore.cart.entity.CartItem;
import com.retailstore.cart.mapper.CartMapper;
import com.retailstore.cart.repository.CartItemRepository;
import com.retailstore.cart.repository.CartRepository;
import com.retailstore.exception.ResourceConflictException;
import com.retailstore.exception.ResourceInsufficientException;
import com.retailstore.exception.ResourceNotFoundException;
import com.retailstore.inventory.entity.Inventory;
import com.retailstore.inventory.repository.InventoryRepository;
import com.retailstore.product.entity.Product;
import com.retailstore.product.repository.ProductRepository;
import com.retailstore.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service implementation responsible for managing shopping cart operations.
 *
 * <p>This service handles the following cart functionalities:</p>
 * <ul>
 *     <li>Adding products to a cart</li>
 *     <li>Removing items from a cart</li>
 *     <li>Fetching cart details for a user</li>
 *     <li>Clearing all items from a cart</li>
 * </ul>
 *
 * <p>The service validates the existence of users, products, and inventory
 * before performing operations to maintain data consistency.</p>
 */
@Service
public class CartServiceImpl implements CartService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Autowired
    public CartServiceImpl(UserRepository userRepository, ProductRepository productRepository,
                           InventoryRepository inventoryRepository, CartRepository cartRepository,
                           CartItemRepository cartItemRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }


    /**
     * Adds a product to the user's cart.
     *
     * <p>If the cart does not exist for the user, a new cart is created.
     * If the product already exists in the cart, its quantity is increased.</p>
     *
     * <p>The method also validates inventory stock before allowing the product
     * to be added or updated in the cart.</p>
     *
     * @param addToCartRequestDTO containing userId, productId and quantity to be added
     * @return updated {@link CartResponseDTO} containing cart details
     *
     * @throws ResourceNotFoundException if user or product does not exist
     * @throws ResourceConflictException if inventory is missing
     * @throws ResourceInsufficientException if requested quantity exceeds available stock
     */
    @Override
    @Transactional
    public CartResponseDTO addToCart(AddToCartRequestDTO addToCartRequestDTO) {

        Long userId = addToCartRequestDTO.getUserId();
        Long productId = addToCartRequestDTO.getProductId();
        int quantity = addToCartRequestDTO.getQuantity();

        validateUser(userId);

        if (!productRepository.existsByIdAndDeletedFalse(productId)){
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() ->
                        new ResourceConflictException("Inventory not found for product: " + productId));

        //Get or create cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });

        //Get or create cart item
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCartId(cart.getId());
                    newItem.setProductId(productId);
                    newItem.setQuantity(0);
                    return newItem;
                });

        int updatedQuantity = cartItem.getQuantity() + quantity;

        if (inventory.getStock() < updatedQuantity){
            throw new ResourceInsufficientException("Total requested quantity: " + updatedQuantity +
                    " exceeds available stock: " + inventory.getStock());
        }

        cartItem.setQuantity(updatedQuantity);

        cartItemRepository.save(cartItem);

        return buildCartResponse(cart);
    }

    /**
     * Removes a specific item from the cart.
     *
     * @param cartItemId ID of the cart item to be removed
     * @return details of the removed {@link CartItemDTO}
     *
     * @throws ResourceNotFoundException if the cart item does not exist
     */
    @Override
    @Transactional
    public CartItemDTO removeFromCart(Long cartItemId) {

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in the cart: " + cartItemId));

        Product product = productRepository.findById(cartItem.getProductId())
                .orElseThrow(() -> new ResourceConflictException(
                        "Product not found for cart item: " + cartItem.getProductId()));

        CartItemDTO cartItemDTO = CartMapper.mapToCartItemDTO(cartItem, product);

        cartItemRepository.delete(cartItem);

        return cartItemDTO;
    }

    /**
     * Retrieves cart details for a specific user.
     *
     * @param userId ID of the user whose cart should be fetched
     * @return {@link CartResponseDTO} containing cart information
     *
     * @throws ResourceNotFoundException if user or cart does not exist
     */
    @Override
    public CartResponseDTO getCartByUser(Long userId) {

        validateUser(userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

        return buildCartResponse(cart);
    }

    /**
     * Removes all items from a cart.
     *
     * @param cartId ID of the cart
     * @return empty cart response
     *
     * @throws ResourceNotFoundException if cart does not exist
     */
    @Override
    @Transactional
    public CartResponseDTO clearCart(Long cartId) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cartId);

        if (cartItems.isEmpty()){
            throw new ResourceConflictException("No item found in the cart: " + cartId);
        }

        cartItemRepository.deleteAll(cartItems);

        return buildCartResponse(cart);
    }

    /**
     * Builds a {@link CartResponseDTO} from a Cart entity.
     *
     * <p>This method retrieves all cart items, fetches corresponding product details,
     * maps them into {@link CartItemDTO}, and calculates the total cart price.</p>
     *
     * @param cart cart entity
     * @return populated {@link CartResponseDTO}
     */
    private CartResponseDTO buildCartResponse(Cart cart){

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        List<Long> productIds = cartItems.stream()
                .map(CartItem::getProductId)
                .toList();

        Map<Long, Product> productMap = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p->p));

        List<CartItemDTO> cartItemDTOs = cartItems.stream()
                .map(item -> {
                    Product p = productMap.get(item.getProductId());
                    if (p == null){
                        throw new ResourceConflictException("Product not found: " + item.getProductId());
                    }
                    return CartMapper.mapToCartItemDTO(item, p);
                })
                .toList();

        BigDecimal totalPrice = cartItemDTOs.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponseDTO.builder()
                .cartId(cart.getId())
                .userId(cart.getUserId())
                .cartItems(cartItemDTOs)
                .totalPrice(totalPrice)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    /**
     * Validates that a user exists.
     */
    private void validateUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }
}
