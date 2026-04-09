package com.retailstore.unit.service;

import com.retailstore.cart.dto.AddToCartRequestDTO;
import com.retailstore.cart.entity.Cart;
import com.retailstore.cart.entity.CartItem;
import com.retailstore.cart.repository.CartItemRepository;
import com.retailstore.cart.repository.CartRepository;
import com.retailstore.cart.service.CartServiceImpl;
import com.retailstore.exception.ResourceConflictException;
import com.retailstore.exception.ResourceInsufficientException;
import com.retailstore.exception.ResourceNotFoundException;
import com.retailstore.inventory.entity.Inventory;
import com.retailstore.inventory.repository.InventoryRepository;
import com.retailstore.product.entity.Product;
import com.retailstore.product.repository.ProductRepository;
import com.retailstore.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    //================<< BUILDERS >>================
    private AddToCartRequestDTO buildAddToCartRequest(){
        AddToCartRequestDTO cartRequest = new AddToCartRequestDTO();
        cartRequest.setUserId(1L);
        cartRequest.setProductId(10L);
        cartRequest.setQuantity(2);

        return cartRequest;
    }

    private Cart buildCart(Long id){
        Cart cart = new Cart();
        cart.setId(id);
        cart.setUserId(1L);

        return cart;
    }

    private CartItem buildCartItem(){
        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setCartId(1L);
        cartItem.setProductId(10L);
        cartItem.setQuantity(2);

        return cartItem;
    }

    private Product buildProduct(){
        Product product = new Product();
        product.setId(10L);
        product.setPrice(BigDecimal.valueOf(100));
        product.setName("Test Product");

        return product;
    }

    private Inventory buildInventory(int stock){
        Inventory inventory = new Inventory();
        inventory.setProductId(10L);
        inventory.setStock(stock);

        return inventory;
    }


    //================<< ADD TO CART >>================
    @Nested
    class AddToCartTests{

        @Test
        void shouldAddToCartSuccessfully(){
            when(userRepository.existsById(1L)).thenReturn(true);
            when(productRepository.existsByIdAndDeletedFalse(10L)).thenReturn(true);
            when(inventoryRepository.findByProductId(10L)).thenReturn(Optional.of(buildInventory(10)));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(buildCart(1L)));
            when(cartItemRepository.findByCartIdAndProductId(1L, 10L))
                    .thenReturn(Optional.empty());
            when(cartItemRepository.save(any()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(buildCartItem()));//buildResponse
            when(productRepository.findAllById(any())).thenReturn(List.of(buildProduct()));//buildResponse

            var response = cartService.addToCart(buildAddToCartRequest());

            assertNotNull(response);
            verify(cartItemRepository).save(any());
        }

        @Test
        void shouldUpdateQuantity_whenProductAlreadyExists(){
            when(userRepository.existsById(1L)).thenReturn(true);
            when(productRepository.existsByIdAndDeletedFalse(10L)).thenReturn(true);
            when(inventoryRepository.findByProductId(10L)).thenReturn(Optional.of(buildInventory(10)));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(buildCart(1L)));

            CartItem existingItem = buildCartItem();

            when(cartItemRepository.findByCartIdAndProductId(1L, 10L))
                    .thenReturn(Optional.of(existingItem));
            when(productRepository.findAllById(any())).thenReturn(List.of(buildProduct()));

            var response = cartService.addToCart(buildAddToCartRequest());

            assertNotNull(response);
            assertEquals(4, existingItem.getQuantity());

            verify(cartItemRepository).save(existingItem);
        }

        @Test
        void shouldCreateCart_whenCartNotExists(){
            when(userRepository.existsById(1L)).thenReturn(true);
            when(productRepository.existsByIdAndDeletedFalse(10L)).thenReturn(true);
            when(inventoryRepository.findByProductId(10L)).thenReturn(Optional.of(buildInventory(10)));

            when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
            when(cartRepository.save(any())).thenAnswer(invocation -> {
                Cart cart = invocation.getArgument(0);
                cart.setId(1L);
                return cart;
            });
            when(cartItemRepository.findByCartIdAndProductId(any(), any()))
                    .thenReturn(Optional.empty());
            when(cartItemRepository.save(any()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(buildCartItem()));
            when(productRepository.findAllById(any())).thenReturn(List.of(buildProduct()));

            var response = cartService.addToCart(buildAddToCartRequest());

            assertNotNull(response);
            assertEquals(1L, response.getUserId());

            verify(cartRepository).save(any());
            verify(cartItemRepository).save(any());
        }
        @Test
        void shouldThrowException_whenUserNotFound(){
            when(userRepository.existsById(1L)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class,
                    () -> cartService.addToCart(buildAddToCartRequest()));
        }

        @Test
        void shouldThrowException_whenProductNotFound(){
            when(userRepository.existsById(1L)).thenReturn(true);
            when(productRepository.existsByIdAndDeletedFalse(10L)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class,
                    () -> cartService.addToCart(buildAddToCartRequest()));
        }

        @Test
        void shouldThrowException_whenInventoryNotFound(){
            when(userRepository.existsById(1L)).thenReturn(true);
            when(productRepository.existsByIdAndDeletedFalse(10L)).thenReturn(true);
            when(inventoryRepository.findByProductId(10L)).thenReturn(Optional.empty());

            assertThrows(ResourceConflictException.class,
                    () -> cartService.addToCart(buildAddToCartRequest()));
        }

        @Test
        void shouldThrowException_whenInSufficientStock(){
            when(userRepository.existsById(1L)).thenReturn(true);
            when(productRepository.existsByIdAndDeletedFalse(10L)).thenReturn(true);
            when(inventoryRepository.findByProductId(10L)).thenReturn(Optional.of(buildInventory(1)));

            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(buildCart(1L)));
            when(cartItemRepository.findByCartIdAndProductId(1L, 10L))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceInsufficientException.class,
                    () -> cartService.addToCart(buildAddToCartRequest()));
        }
    }

    //================<< GET CART BY USER >>================
    @Nested
    class GetCartByUserTests{

        @Test
        void shouldGetCartByUserSuccessfully(){
            when(userRepository.existsById(1L)).thenReturn(true);
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(buildCart(1L)));
            when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(buildCartItem()));

            when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(buildCartItem()));//buildResponse
            when(productRepository.findAllById(any())).thenReturn(List.of(buildProduct()));//buildResponse

            assertFalse(cartService.getCartByUser(1L).getCartItems().isEmpty());
        }

        @Test
        void shouldThrowException_whenUserNotFound(){
            when(userRepository.existsById(1L)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class,
                    () -> cartService.getCartByUser(1L));
        }

        @Test
        void shouldThrowException_whenCartNotFound(){
            when(userRepository.existsById(1L)).thenReturn(true);
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> cartService.getCartByUser(1L));
        }
    }

    //================<< REMOVE CART ITEM >>================
    @Nested
    class RemoveCartItemTests{

        @Test
        void shouldRemoveCartItemSuccessfully(){
            CartItem cartItem = buildCartItem();

            when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));
            when(productRepository.findById(cartItem.getProductId())).thenReturn(Optional.of(buildProduct()));

            cartService.removeFromCart(cartItem.getId());

            verify(cartItemRepository).delete(cartItem);
        }

        @Test
        void showThrowException_whenItemNotFound(){
            when(cartItemRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> cartService.removeFromCart(1L));
        }

        @Test
        void showThrowException_whenProductNotFound(){
            CartItem cartItem = buildCartItem();

            when(cartItemRepository.findById(1L)).thenReturn(Optional.of(buildCartItem()));
            when(productRepository.findById(cartItem.getProductId())).thenReturn(Optional.empty());

            assertThrows(ResourceConflictException.class,
                    () -> cartService.removeFromCart(1L));
        }
    }

    //================<< CLEAR CART >>================
    @Nested
    class ClearCartTests{

        @Test
        void shouldClearCartSuccessfully(){
            Cart cart = buildCart(3L);
            CartItem cartItem = buildCartItem();

            when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of(cartItem));

            when(productRepository.findAllById(any())).thenReturn(List.of(buildProduct()));//buildResponse

            cartService.clearCart(cart.getId());

            verify(cartItemRepository).deleteAll(any());
        }

        @Test
        void shouldThrowException_whenCartNotFound(){
            when(cartRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> cartService.clearCart(1L));
        }

        @Test
        void shouldThrowException_whenCartIsEmpty(){
            Cart cart = buildCart(1L);

            when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of());

            assertThrows(ResourceConflictException.class,
                    () -> cartService.clearCart(cart.getId()));
        }
    }
}
