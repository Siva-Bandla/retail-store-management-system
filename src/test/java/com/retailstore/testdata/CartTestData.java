package com.retailstore.testdata;

import com.retailstore.cart.entity.Cart;
import com.retailstore.cart.entity.CartItem;
import com.retailstore.cart.repository.CartItemRepository;
import com.retailstore.cart.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class CartTestData {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    public Cart createCart(Long userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);

        return cartRepository.save(cart);
    }

    public CartItem createCartItem(Long cartId, Long productId, int qty) {
        CartItem cartItem = new CartItem();
        cartItem.setCartId(cartId);
        cartItem.setProductId(productId);
        cartItem.setQuantity(qty);

        return cartItemRepository.save(cartItem);
    }
}