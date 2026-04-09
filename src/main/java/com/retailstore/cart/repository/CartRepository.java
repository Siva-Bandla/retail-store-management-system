package com.retailstore.cart.repository;

import com.retailstore.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);

    boolean existsByIdAndUserId(Long cartId, Long userId);
}
