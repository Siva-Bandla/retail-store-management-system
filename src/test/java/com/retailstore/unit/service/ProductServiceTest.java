package com.retailstore.unit.service;

import com.retailstore.category.entity.Category;
import com.retailstore.category.repository.CategoryRepository;
import com.retailstore.exception.ResourceNotFoundException;
import com.retailstore.inventory.entity.Inventory;
import com.retailstore.inventory.repository.InventoryRepository;
import com.retailstore.product.dto.ProductRequestDTO;
import com.retailstore.product.dto.ProductResponseDTO;
import com.retailstore.product.entity.Product;
import com.retailstore.product.repository.ProductRepository;
import com.retailstore.product.service.ProductServiceImpl;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private InventoryRepository inventoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    //================<< BUILDERS >>================
    private ProductRequestDTO buildProductRequest(){
        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("Test product");
        request.setDescription("Product Description");
        request.setCategoryId(1L);
        request.setPrice(BigDecimal.valueOf(100));
        request.setQuantity(5);

        return request;
    }

    private Product buildProduct(Long id){
        Product product = new Product();
        product.setId(id);
        product.setName("Test Product");
        product.setCategoryId(1L);
        product.setPrice(BigDecimal.valueOf(100));

        return product;
    }

    private Inventory buildInventory(Long productId, int stock){
        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setStock(stock);

        return inventory;
    }

    private void mockProductSave(){
        when(productRepository.save(any())).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            if (product.getId() == null)
                product.setId(10L);
            return product;
        });
    }

    //================<< CREATE PRODUCT >>================
    @Nested
    class CreateProductTests{

        @Test
        void shouldCreateProductSuccessfully(){
            when(categoryRepository.existsById(1L)).thenReturn(true);
            mockProductSave();

            when(inventoryRepository.findByProductId(10L))
                    .thenReturn(Optional.empty());
            when(inventoryRepository.save(any()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ProductResponseDTO response = productService.createProduct(buildProductRequest());

            assertNotNull(response);

            verify(productRepository).save(any());
            verify(inventoryRepository).save(any());
        }

        @Test
        void shouldCreateProductAndUpdateExistingInventory(){
            when(categoryRepository.existsById(1L)).thenReturn(true);
            mockProductSave();

            when(inventoryRepository.findByProductId(10L))
                    .thenReturn(Optional.of(buildInventory(10L, 5)));
            when(inventoryRepository.save(any()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ProductResponseDTO response = productService.createProduct(buildProductRequest());

            assertNotNull(response);

            verify(productRepository).save(any());
            verify(inventoryRepository).save(argThat(inv -> inv.getStock() == 10));
        }

        @Test
        void shouldThrowException_whenCategoryNotFound(){
            when(categoryRepository.existsById(1L)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class,
                    () -> productService.createProduct(buildProductRequest()));
        }
    }

    //================<< GET ALL PRODUCTS >>================
    @Nested
    class GetAllProductsTests{

        @Test
        void shouldGetAllProductsSuccessfully(){
            when(productRepository.findAllByDeletedFalse())
                    .thenReturn(List.of(buildProduct(100L)));
            when(inventoryRepository.findByProductIdInAndDeletedFalse(List.of(100L)))
                    .thenReturn(List.of(buildInventory(100L, 9)));

            List<ProductResponseDTO> responses = productService.getAllProducts();

            assertEquals(1, responses.size());
        }

        @Test
        void shouldReturnEmptyList(){
            when(productRepository.findAllByDeletedFalse())
                    .thenReturn(List.of());

            List<ProductResponseDTO> responses = productService.getAllProducts();

            assertTrue(responses.isEmpty());
        }

        @Test
        void shouldReturnZeroStock_whenInventoryMissing(){
            when(productRepository.findAllByDeletedFalse())
                    .thenReturn(List.of(buildProduct(10L)));
            when(inventoryRepository.findByProductIdInAndDeletedFalse(List.of(10L)))
                    .thenReturn(List.of());

            List<ProductResponseDTO> responses = productService.getAllProducts();

            assertEquals(1, responses.size());
            assertEquals(0, responses.getFirst().getQuantity());

            verify(inventoryRepository).findByProductIdInAndDeletedFalse(any());
        }
    }

    //================<< GET PRODUCT BY ID >>================
    @Nested
    class GetProductByIdTests{

        @Test
        void shouldGetProductByIdSuccessfully(){
            when(productRepository.findByIdAndDeletedFalse(10L))
                    .thenReturn(Optional.of(buildProduct(10L)));

            ProductResponseDTO response = productService.getProductById(10L);

            assertNotNull(response);
        }
        
        @Test
        void showThrowException_whenProductNotFound(){
            when(productRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> productService.getProductById(10L));
        }
    }

    //================<< UPDATE PRODUCT >>================
    @Nested
    class UpdateProductTests{

        @Test
        void shouldUpdateProductSuccessfully(){
            when(productRepository.findByIdAndDeletedFalse(2L))
                    .thenReturn(Optional.of(buildProduct(2L)));
            when(categoryRepository.existsById(1L)).thenReturn(true);
            when(productRepository.save(any()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ProductResponseDTO response = productService.updateProduct(2L, buildProductRequest());

            assertNotNull(response);

            verify(productRepository).save(any());
        }

        @Test
        void shouldThrowException_whenProductNotFound(){
            when(productRepository.findByIdAndDeletedFalse(3L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> productService.updateProduct(3L, buildProductRequest()));
        }

        @Test
        void shouldThrowException_whenCategoryNotFound(){
            when(productRepository.findByIdAndDeletedFalse(4L))
                    .thenReturn(Optional.of(buildProduct(4L)));
            when(categoryRepository.existsById(1L)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class,
                    () -> productService.updateProduct(4L, buildProductRequest()));
        }
    }

    //================<< DELETE PRODUCT >>================
    @Nested
    class DeleteProductTests{

        @Test
        void shouldDeleteProductSuccessfully(){
            when(productRepository.findByIdAndDeletedFalse(5L))
                    .thenReturn(Optional.of(buildProduct(5L)));
            when(productRepository.save(any()))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(inventoryRepository.findByProductIdAndDeletedFalse(5L))
                    .thenReturn(Optional.of(buildInventory(5L, 4)));
            when(inventoryRepository.save(any()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ProductResponseDTO response = productService.deleteProduct(5L);

            assertNotNull(response);

            verify(productRepository).save(any());
            verify(inventoryRepository).save(any());
        }

        @Test
        void shouldDeleteProduct_withoutInventory(){
            when(productRepository.findByIdAndDeletedFalse(5L))
                    .thenReturn(Optional.of(buildProduct(5L)));
            when(productRepository.save(any()))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(inventoryRepository.findByProductIdAndDeletedFalse(5L))
                    .thenReturn(Optional.empty());

            ProductResponseDTO response = productService.deleteProduct(5L);

            assertNotNull(response);

            verify(productRepository).save(any());
            verify(inventoryRepository, never()).save(any());
        }

        @Test
        void shouldThrowException_whenProductNotFound(){
            when(productRepository.findByIdAndDeletedFalse(6L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> productService.deleteProduct(6L));
        }
    }
}
