package com.retailstore.security;

import com.retailstore.cart.repository.CartItemRepository;
import com.retailstore.cart.repository.CartRepository;
import com.retailstore.security.userdetails.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CartSecurity {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Autowired
    public CartSecurity(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public boolean isOwnerByUserId(Long userId, Authentication authentication){

        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)){
            return false;
        }

        return userDetails.getId().equals(userId);
    }

    public boolean isOwnerByCartId(Long cartId, Authentication authentication){

        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)){
            return false;
        }

        return cartRepository.existsByIdAndUserId(cartId, userDetails.getId());
    }

    public boolean isOwnerByCartItemId(Long cartItemId, Authentication authentication){

        if (!(authentication.getPrincipal() instanceof  CustomUserDetails userDetails)){
            return false;
        }

        return cartItemRepository.existsByIdAndCartUserId(cartItemId, userDetails.getId());
    }
}
