package com.retailstore.cart.service;

import com.retailstore.cart.dto.AddToCartRequestDTO;
import com.retailstore.cart.dto.CartItemDTO;
import com.retailstore.cart.dto.CartResponseDTO;

public interface CartService {

    CartResponseDTO addToCart(AddToCartRequestDTO addToCartRequestDTO);
    CartItemDTO removeFromCart(Long cartItemId);
    CartResponseDTO getCartByUser(Long userId);
    CartResponseDTO clearCart(Long cartId);
}
