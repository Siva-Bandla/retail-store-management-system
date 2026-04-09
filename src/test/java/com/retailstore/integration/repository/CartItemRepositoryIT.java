package com.retailstore.integration.repository;

import com.retailstore.cart.entity.Cart;
import com.retailstore.cart.entity.CartItem;
import com.retailstore.cart.repository.CartItemRepository;
import com.retailstore.cart.repository.CartRepository;
import com.retailstore.testdata.CartTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(CartTestData.class)
public class CartItemRepositoryIT {

    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CartTestData cartTestData;

    @Test
    @DisplayName("Save a cart item and verify persistence")
    void testSaveCart(){
        Cart cart = cartTestData.createCart(1L);
        CartItem item = cartTestData.createCartItem(cart.getId(), 10L, 3);

        assertNotNull(item.getId());
        assertEquals(10L, item.getProductId());
        assertEquals(3, item.getQuantity());
    }

    //-------------------------------------------------------------------
    @Test
    @DisplayName("Find by cartId and productId")
    void testFindByCartIdAndProductId(){
        Cart cart = cartTestData.createCart(2L);
        CartItem item = cartTestData.createCartItem(cart.getId(), 200L, 5);

        Optional<CartItem> found = cartItemRepository.findByCartIdAndProductId(cart.getId(), 200L);

        assertTrue(found.isPresent());
        assertEquals(5, found.get().getQuantity());
    }

    @Test
    @DisplayName("Find by cartId and productId return empty when not found")
    void testFindByCartIdAndProductIdNotFound(){
        Cart cart = cartTestData.createCart(2L);

        Optional<CartItem> found = cartItemRepository.findByCartIdAndProductId(cart.getId(), 999L);

       assertTrue(found.isEmpty());
    }

    //----------------------------------------------------------------
    @Test
    @DisplayName("Find all items by cartId")
    void testFindCartByUserId(){
        Cart cart = cartTestData.createCart(3L);

        cartTestData.createCartItem(cart.getId(), 11L, 1);
        cartTestData.createCartItem(cart.getId(), 12L, 2);
        cartTestData.createCartItem(cart.getId(), 13L, 3);

        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        assertEquals(3, items.size());
    }

    //----------------------------------------------------------------
    @Test
    @DisplayName("existsByIdAndCartUserId return true")
    void testExistsByIdAndCartUserIdTrue(){
        Cart cart = cartTestData.createCart(5L);
        CartItem item = cartTestData.createCartItem(cart.getId(), 500L, 2);

        boolean exists = cartItemRepository.existsByIdAndCartUserId(item.getId(), 5L);

        assertTrue(exists);
    }

    @Test
    @DisplayName("existsByIdAndCartUserId return false when cartItemId does not exist")
    void testExistsByIdAndCartUserIdItemNotFound(){

        boolean exists = cartItemRepository.existsByIdAndCartUserId(999L, 5L);

        assertFalse(exists);
    }
}
