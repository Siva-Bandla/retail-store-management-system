package com.retailstore.cart.controller;

import com.retailstore.cart.dto.AddToCartRequestDTO;
import com.retailstore.cart.dto.CartItemDTO;
import com.retailstore.cart.dto.CartResponseDTO;
import com.retailstore.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller responsible for managing shopping cart operations.
 *
 * <p>Provides endpoints for adding items to the cart, removing items,
 * retrieving a user's cart, and clearing the cart.</p>
 */
@RestController
@RequestMapping("/carts")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Adds a product to the user's cart.
     *
     * <p>If the cart does not exist, a new cart is created.
     * If the product already exists in the cart, its quantity is updated.</p>
     *
     * @param addToCartRequestDTO request containing userId, productId, and quantity
     * @return updated cart details
     */
    @PreAuthorize("hasRole('CUSTOMER') and #addToCartRequestDTO.userId == authentication.principal.id")
    @PostMapping("/add")
    public ResponseEntity<CartResponseDTO> addToCart(@RequestBody AddToCartRequestDTO addToCartRequestDTO){

        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addToCart(addToCartRequestDTO));
    }

    /**
     * Removes a specific item from the cart.
     *
     * @param cartItemId ID of the cart item to remove
     * @return details of the removed cart item
     */
    @PreAuthorize("hasRole('CUSTOMER') and @cartSecurity.isOwnerByCartItemId(#cartItemId, authentication)")
    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<CartItemDTO> removeFromCart(@PathVariable Long cartItemId){

        return ResponseEntity.ok(cartService.removeFromCart(cartItemId));
    }

    /**
     * Retrieves the cart for a specific user.
     *
     * @param userId ID of the user
     * @return cart details including items and total price
     */
    @PreAuthorize("hasRole('ADMIN') or @cartSecurity.isOwnerByUserId(#userId, authentication)")
    @GetMapping("/user/{userId}")
    public ResponseEntity<CartResponseDTO> getCartByUser(@PathVariable Long userId){

        return ResponseEntity.ok(cartService.getCartByUser(userId));
    }

    /**
     * Removes all items from the specified cart.
     *
     * @param cartId ID of the cart to clear
     * @return updated cart details after removal
     */
    @PreAuthorize("hasRole('ADMIN') or @cartSecurity.isOwnerByCartId(#cartId, authentication)")
    @DeleteMapping("/clear/{cartId}")
    public ResponseEntity<CartResponseDTO> clearCart(@PathVariable Long cartId){

        return ResponseEntity.ok(cartService.clearCart(cartId));
    }

}
