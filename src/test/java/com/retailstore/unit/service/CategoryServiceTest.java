package com.retailstore.unit.service;

import com.retailstore.category.dto.CategoryRequestDTO;
import com.retailstore.category.dto.CategoryResponseDTO;
import com.retailstore.category.entity.Category;
import com.retailstore.category.repository.CategoryRepository;
import com.retailstore.category.service.CategoryServiceImpl;
import com.retailstore.exception.ResourceConflictException;
import com.retailstore.exception.ResourceNotFoundException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    //================<< BUILDERS >>================
    private CategoryRequestDTO buildCategoryRequest(){
        CategoryRequestDTO request = new CategoryRequestDTO();
        request.setName("Test");
        request.setDescription("Test Category");

        return request;
    }

    private Category buildCategory(){
        Category category = new Category();
        category.setId(2L);
        category.setName("Test");
        category.setDescription("Test Category");

        return category;
    }

    //================<< CREATE CATEGORY >>================
    @Nested
    class CreateCategoryTests{

        @Test
        void shouldCreateCategorySuccessfully(){
            when(categoryRepository.existsByName("Test")).thenReturn(false);
            when(categoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            CategoryResponseDTO response = categoryService.createCategory(buildCategoryRequest());

            assertNotNull(response);

            verify(categoryRepository).save(any());
        }

        @Test
        void shouldThrowException_whenCategoryAlreadyExists(){
            when(categoryRepository.existsByName("Test")).thenReturn(true);

            assertThrows(ResourceConflictException.class,
                    () -> categoryService.createCategory(buildCategoryRequest()));
        }
    }

    //================<< GET ALL CATEGORIES >>================
    @Nested
    class GetAllCategoriesTests{

        @Test
        void shouldGetAllCategoriesSuccessfully(){
            when(categoryRepository.findAll()).thenReturn(List.of(buildCategory()));

            List<CategoryResponseDTO> response = categoryService.getAllCategories();

            assertFalse(response.isEmpty());
            assertEquals(1, response.size());
        }

        @Test
        void shouldGetEmptyList_whenNoCategoriesFound(){
            when(categoryRepository.findAll()).thenReturn(List.of());

            List<CategoryResponseDTO> response = categoryService.getAllCategories();

            assertTrue(response.isEmpty());
        }
    }

    //================<< GET CATEGORY BY ID>>================
    @Nested
    class GetCategoryByIdTests{

        @Test
        void shouldGetCategoryByIdSuccessfully(){
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(buildCategory()));

            CategoryResponseDTO response = categoryService.getCategoryById(2L);

            assertNotNull(response);
            assertEquals(2L, response.getId());
        }

        @Test
        void shouldThrowException_whenCategoryNotFound(){
            when(categoryRepository.findById(2L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> categoryService.getCategoryById(2L));
        }
    }

    //================<< UPDATE CATEGORY >>================
    @Nested
    class UpdateCategoryTests{

        @Test
        void shouldUpdateCategorySuccessfully(){
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(buildCategory()));
            when(categoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            CategoryResponseDTO response = categoryService.updateCategory(2L, buildCategoryRequest());

            assertNotNull(response);

            verify(categoryRepository).save(any());
        }

        @Test
        void shouldThrowExceptionWhenCategoryNotFound(){
            when(categoryRepository.findById(2L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> categoryService.updateCategory(2L, buildCategoryRequest()));
        }
    }

    //================<< DELETE CATEGORY >>================
    @Nested
    class DeleteCategoryTests{

        @Test
        void shouldDeleteCategorySuccessfully(){
            Category category = buildCategory();

            when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
            when(productRepository.existsByCategoryIdAndDeletedFalse(2L)).thenReturn(false);

            CategoryResponseDTO response = categoryService.deleteCategory(2L);

            assertNotNull(response);

            verify(categoryRepository).delete(category);
        }

        @Test
        void shouldThrowException_whenCategoryNotFound(){
            when(categoryRepository.findById(2L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> categoryService.deleteCategory(2L));
        }

        @Test
        void shouldThrowException_whenProductHasCategory(){
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(buildCategory()));
            when(productRepository.existsByCategoryIdAndDeletedFalse(2L)).thenReturn(true);

            assertThrows(ResourceConflictException.class,
                    () -> categoryService.deleteCategory(2L));
        }
    }

}
