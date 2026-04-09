package com.retailstore.unit.service;

import com.retailstore.exception.ResourceConflictException;
import com.retailstore.exception.ResourceNotFoundException;
import com.retailstore.inventory.dto.InventoryRequestDTO;
import com.retailstore.inventory.dto.InventoryResponseDTO;
import com.retailstore.inventory.entity.Inventory;
import com.retailstore.inventory.repository.InventoryRepository;
import com.retailstore.inventory.service.InventoryServiceImpl;
import com.retailstore.product.repository.ProductRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    //================<< BUILDERS >>================
    private InventoryRequestDTO buildInventoryRequest(){
        InventoryRequestDTO request = new InventoryRequestDTO();
        request.setProductId(2L);
        request.setStock(5);

        return request;
    }

    private Inventory buildInventory(Long productId, int stock){
        Inventory inventory = new Inventory();
        inventory.setId(4L);
        inventory.setProductId(productId);
        inventory.setStock(stock);

        return inventory;
    }

    //================<< CREATE INVENTORY >>================
    @Nested
    class CreateInventoryTests{

        @Test
        void shouldCreateInventorySuccessfully(){
            when(inventoryRepository.existsByProductIdAndDeletedFalse(2L)).thenReturn(false);
            when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            InventoryResponseDTO response = inventoryService.createInventory(buildInventoryRequest());

            assertNotNull(response);

            verify(inventoryRepository).save(any());
        }

//        @Test
//        void shouldThrowException_whenStockLessThanZero(){
//            InventoryRequestDTO request = buildInventoryRequest();
//            request.setStock(-4);
//            when(inventoryRepository.existsByProductIdAndDeletedFalse(8L)).thenReturn(false);
//
//            InventoryResponseDTO response = inventoryService.createInventory(request);
//
//            assertNull(response);
//
//            assertThrows(IllegalArgumentException.class,
//                    () -> inventoryService.createInventory(request));
//        }

        @Test
        void shouldThrowException_whenInventoryAlreadyExists(){
            when(inventoryRepository.existsByProductIdAndDeletedFalse(2L)).thenReturn(true);

            assertThrows(ResourceConflictException.class,
                    () -> inventoryService.createInventory(buildInventoryRequest()));
        }
    }

    //================<< UPDATE INVENTORY >>================
    @Nested
    class UpdateInventoryTests{

        @Test
        void shouldUpdateInventorySuccessfully(){
            when(inventoryRepository.findByIdAndDeletedFalse(4L))
                    .thenReturn(Optional.of(buildInventory(2L, 5)));
            when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            InventoryResponseDTO response = inventoryService
                    .updateInventory(4L, buildInventoryRequest());

            assertNotNull(response);

            verify(inventoryRepository).save(argThat(i -> i.getStock() == 10));
        }

        @Test
        void shouldThrowException_whenInventoryNotFound(){
            when(inventoryRepository.findByIdAndDeletedFalse(8L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> inventoryService.updateInventory(8L, buildInventoryRequest()));
        }

        @Test
        void shouldThrowException_whenProductAlreadyExists(){

            when(inventoryRepository.findByIdAndDeletedFalse(2L))
                    .thenReturn(Optional.of(buildInventory(2L, 5)));

            InventoryRequestDTO request = buildInventoryRequest();
            request.setProductId(3L);

            assertThrows(ResourceConflictException.class,
                    () -> inventoryService.updateInventory(2L, request));
        }

        @Test
        void shouldThrowException_whenStockLessThanOne(){
            when(inventoryRepository.findByIdAndDeletedFalse(4L))
                    .thenReturn(Optional.of(buildInventory(2L, 5)));

            InventoryRequestDTO request = buildInventoryRequest();
            request.setStock(0);

            assertThrows(IllegalArgumentException.class,
                    () -> inventoryService.updateInventory(4L, request));
        }
    }

    //================<< GET ALL INVENTORIES >>================
    @Nested
    class GetAllInventoriesTests{

        @Test
        void shouldGetAllInventoriesSuccessfully(){
            when(inventoryRepository.findAll()).thenReturn(List.of(buildInventory(4L, 99)));

            List<InventoryResponseDTO> responses = inventoryService.getAllInventories();

            assertFalse(responses.isEmpty());
            assertEquals(1, responses.size());
        }

        @Test
        void shouldGetEmptyList_whenNoInventoriesFound(){
            when(inventoryRepository.findAll()).thenReturn(List.of());

            List<InventoryResponseDTO> responses = inventoryService.getAllInventories();

            assertTrue(responses.isEmpty());
        }
    }

    //================<< GET INVENTORY BY PRODUCT ID >>================
    @Nested
    class GetInventoryByProductIdTests{

        @Test
        void shouldGetInventoryByProductIdSuccessfully(){
            when(productRepository.existsByIdAndDeletedFalse(7L)).thenReturn(true);
            when(inventoryRepository.findByProductIdAndDeletedFalse(7L))
                    .thenReturn(Optional.of(buildInventory(7L, 7)));

            InventoryResponseDTO response = inventoryService.getInventoryByProductId(7L);

            assertNotNull(response);
        }

        @Test
        void shouldThrowException_whenProductNotFound(){
            when(productRepository.existsByIdAndDeletedFalse(7L)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class,
                    () -> inventoryService.getInventoryByProductId(7L));
        }

        @Test
        void shouldThrowException_whenInventoryNotFound(){
            when(productRepository.existsByIdAndDeletedFalse(7L)).thenReturn(true);
            when(inventoryRepository.findByProductIdAndDeletedFalse(7L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> inventoryService.getInventoryByProductId(7L));
        }
    }

    //================<< DEACTIVATE INVENTORY >>================
    @Nested
    class DeactivateInventoryTests{

        @Test
        void shouldDeactivateInventorySuccessfully(){
            when(inventoryRepository.findByIdAndDeletedFalse(4L))
                    .thenReturn(Optional.of(buildInventory(6L, 23)));
            when(productRepository.existsByIdAndDeletedFalse(6L)).thenReturn(false);
            when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            InventoryResponseDTO response = inventoryService.deactivateInventory(4L);

            assertNotNull(response);

            verify(inventoryRepository).save(argThat(i -> i.getDeleted() == true));
        }

        @Test
        void shouldThrowException_whenInventoryNotFound(){
            when(inventoryRepository.findByIdAndDeletedFalse(4L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> inventoryService.deactivateInventory(4L));
        }

        @Test
        void shouldThrowException_whenInventoryHasProducts(){
            when(inventoryRepository.findByIdAndDeletedFalse(4L))
                    .thenReturn(Optional.of(buildInventory(6L, 23)));
            when(productRepository.existsByIdAndDeletedFalse(6L)).thenReturn(true);

            assertThrows(ResourceConflictException.class,
                    () -> inventoryService.deactivateInventory(4L));
        }
    }
}
