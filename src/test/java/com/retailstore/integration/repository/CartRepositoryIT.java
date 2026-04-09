package com.retailstore.integration.repository;

import com.retailstore.cart.entity.Cart;
import com.retailstore.cart.repository.CartRepository;
import com.retailstore.testdata.CartTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(CartTestData.class)
public class CartRepositoryIT {

    @Autowired private CartRepository cartRepository;
    @Autowired private CartTestData cartTestData;

    @Test
    @DisplayName("Save a cart and verify persistence")
    void testSaveCart(){
        Cart cart = cartTestData.createCart(1L);
        assertNotNull(cart.getId());
        assertEquals(1L, cart.getUserId());
    }

    @Test
    @DisplayName("Find cart by Id")
    void testFindCartById(){
        Cart cart = cartTestData.createCart(2L);
        Optional<Cart> found = cartRepository.findById(cart.getId());

        assertTrue(found.isPresent());
        assertEquals(2L, found.get().getUserId());
    }

    @Test
    @DisplayName("Find cart by userId")
    void testFindCartByUserId(){
        Cart cart = cartTestData.createCart(3L);
        Optional<Cart> found = cartRepository.findByUserId(cart.getUserId());

        assertTrue(found.isPresent());
        assertEquals(3L, found.get().getUserId());
    }

    @Test
    @DisplayName("existsByIdAndUserId")
    void testExistsByIdAndUserId(){
        Cart cart = cartTestData.createCart(4L);

        boolean exists = cartRepository.existsByIdAndUserId(cart.getId(), 4L);

        assertTrue(exists);
    }

    @Test
    @DisplayName("Delete a cart")
    void testDeleteCart(){
        Cart cart = cartTestData.createCart(4L);
        cartRepository.delete(cart);

        Optional<Cart> found = cartRepository.findById(cart.getId());
        assertFalse(found.isPresent());
    }
}
