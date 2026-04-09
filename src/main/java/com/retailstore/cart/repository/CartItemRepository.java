package com.retailstore.cart.repository;

import com.retailstore.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    List<CartItem> findByCartId(Long cartId);

    @Query(value = """
            SELECT COUNT(*) > 0
            FROM cart_items ci
            JOIN carts c ON ci.cart_id = c.cart_id
            WHERE ci.cart_item_id = :cartItemId
              AND c.user_id = :userId
            """, nativeQuery = true)
    boolean existsByIdAndCartUserId(Long cartItemId, Long userId);
}
