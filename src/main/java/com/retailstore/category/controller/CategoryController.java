package com.retailstore.category.controller;

import com.retailstore.category.dto.CategoryRequestDTO;
import com.retailstore.category.dto.CategoryResponseDTO;
import com.retailstore.category.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller responsible for managing category-related operations.
 *
 * <p>This controller exposes endpoints for creating, retrieving,
 * updating, and deleting product categories in the retail store system.</p>
 *
 * <p>All operations delegate business logic to the {@link CategoryService}
 * and return standardized HTTP responses.</p>
 */
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * REST controller responsible for managing category-related operations.
     *
     * <p>This controller exposes endpoints for creating, retrieving,
     * updating, and deleting product categories in the retail store system.</p>
     *
     * <p>All operations delegate business logic to the {@link CategoryService}
     * and return standardized HTTP responses.</p>
     */
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryRequestDTO categoryRequestDTO){

        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(categoryRequestDTO));
    }

    /**
     * Retrieves all categories available in the system.
     *
     * @return {@link ResponseEntity} containing a list of {@link CategoryResponseDTO}
     *         with HTTP status 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories(){

        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    /**
     * Retrieves a category by its unique identifier.
     *
     * @param categoryId the ID of the category to retrieve
     * @return {@link ResponseEntity} containing the {@link CategoryResponseDTO}
     *         with HTTP status 200 (OK)
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable Long categoryId){

        return ResponseEntity.ok(categoryService.getCategoryById(categoryId));
    }

    /**
     * Updates an existing category.
     *
     * @param categoryId the ID of the category to update
     * @param categoryRequestDTO request object containing updated category details
     * @return {@link ResponseEntity} containing the updated {@link CategoryResponseDTO}
     *         with HTTP status 200 (OK)
     */
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable Long categoryId,
                                                              @Valid @RequestBody CategoryRequestDTO categoryRequestDTO){

        return ResponseEntity.ok(categoryService.updateCategory(categoryId, categoryRequestDTO));
    }

    /**
     * Deletes a category from the system.
     *
     * <p>If the category is associated with existing products,
     * a conflict exception may be thrown.</p>
     *
     * @param categoryId the ID of the category to delete
     * @return {@link ResponseEntity} containing the deleted {@link CategoryResponseDTO}
     *         with HTTP status 200 (OK)
     */
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> deleteCategory(@PathVariable Long categoryId){

        return ResponseEntity.ok(categoryService.deleteCategory(categoryId));
    }
}
