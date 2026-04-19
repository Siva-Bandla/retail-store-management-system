package com.retailstore.cart.repository;

import com.retailstore.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    List<CartItem> findByCartId(Long cartId);

    @Query("""
        SELECT COUNT(ci) > 0
        FROM CartItem ci
        JOIN Cart c ON ci.cartId = c.id
        WHERE ci.id = :cartItemId AND c.userId = :userId
    """)
    boolean existsByIdAndCartUserId(Long cartItemId, Long userId);
}
