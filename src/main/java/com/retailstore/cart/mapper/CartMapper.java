package com.retailstore.cart.mapper;

import com.retailstore.cart.dto.CartItemDTO;
import com.retailstore.cart.entity.CartItem;
import com.retailstore.product.entity.Product;

public class CartMapper {

    private CartMapper(){}

    public static CartItemDTO mapToCartItemDTO(CartItem item, Product product){

        return CartItemDTO.builder()
                .cartItemId(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .price(product.getPrice())
                .quantity(item.getQuantity())
                .build();
    }

}
