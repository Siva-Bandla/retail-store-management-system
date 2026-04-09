package com.retailstore.category.service;

import com.retailstore.category.dto.CategoryRequestDTO;
import com.retailstore.category.dto.CategoryResponseDTO;
import com.retailstore.category.entity.Category;
import com.retailstore.category.mapper.CategoryMapper;
import com.retailstore.category.repository.CategoryRepository;
import com.retailstore.exception.ResourceConflictException;
import com.retailstore.exception.ResourceNotFoundException;
import com.retailstore.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation responsible for handling business logic
 * related to {@link Category} operations.
 *
 * <p>This service provides functionality to create, retrieve,
 * update, and delete categories in the retail store system.</p>
 */
@Service
public class CategoryServiceImpl implements CategoryService{

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }


    /**
     * Creates a new category in the system.
     *
     * @param categoryRequestDTO DTO containing category details such as name and description
     * @return {@link CategoryResponseDTO} containing the saved category information
     */
    @Override
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO categoryRequestDTO) {

        String name = categoryRequestDTO.getName();

        if(categoryRepository.existsByName(name)){
            throw new ResourceConflictException("Category already exists with: " + name);

        }

        Category category = new Category();
        category.setName(name);
        category.setDescription(categoryRequestDTO.getDescription());
        Category savedCategory = categoryRepository.save(category);

        return CategoryMapper.mapToCategoryResponseDTO(savedCategory);
    }

    /**
     * Retrieves all categories available in the system.
     *
     * @return List of {@link CategoryResponseDTO} representing all categories
     */
    @Override
    public List<CategoryResponseDTO> getAllCategories() {

        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(CategoryMapper::mapToCategoryResponseDTO)
                .toList();
    }

    /**
     * Retrieves a category by its unique ID.
     *
     * @param categoryId ID of the category
     * @return {@link CategoryResponseDTO} representing the category
     * @throws ResourceNotFoundException if the category does not exist
     */
    @Override
    public CategoryResponseDTO getCategoryById(Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with category id: " + categoryId
                ));
        return CategoryMapper.mapToCategoryResponseDTO(category);
    }

    /**
     * Updates an existing category.
     *
     * @param categoryId ID of the category to update
     * @param categoryRequestDTO DTO containing updated category details
     * @return {@link CategoryResponseDTO} representing the updated category
     * @throws ResourceNotFoundException if the category does not exist
     */
    @Override
    @Transactional
    public CategoryResponseDTO updateCategory(Long categoryId, CategoryRequestDTO categoryRequestDTO) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + categoryId
                ));
        category.setName(categoryRequestDTO.getName());
        category.setDescription(categoryRequestDTO.getDescription());
        Category savedCategory = categoryRepository.save(category);

        return CategoryMapper.mapToCategoryResponseDTO(savedCategory);
    }

    /**
     * Deletes a category from the system.
     *
     * <p>Before deleting, the method verifies that no products are
     * associated with the category. If products exist, deletion
     * is prevented to maintain data integrity.</p>
     *
     * @param categoryId ID of the category to delete
     * @return {@link CategoryResponseDTO} representing the deleted category
     * @throws ResourceNotFoundException if the category does not exist
     * @throws IllegalStateException if products are associated with the category
     */
    @Override
    @Transactional
    public CategoryResponseDTO deleteCategory(Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Category not found with id: " + categoryId
                ));
        if (productRepository.existsByCategoryIdAndDeletedFalse(categoryId)){
            throw new ResourceConflictException(
                    "Cannot delete category. Products are associated with this category: " + categoryId
            );
        }
        CategoryResponseDTO categoryResponseDTO = CategoryMapper.mapToCategoryResponseDTO(category);
        categoryRepository.delete(category);

        return categoryResponseDTO;
    }
}
