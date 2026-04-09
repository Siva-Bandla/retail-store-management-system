package com.retailstore.category.service;

import com.retailstore.category.dto.CategoryRequestDTO;
import com.retailstore.category.dto.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {

    CategoryResponseDTO createCategory(CategoryRequestDTO categoryRequestDTO);
    List<CategoryResponseDTO> getAllCategories();
    CategoryResponseDTO getCategoryById(Long categoryId);
    CategoryResponseDTO updateCategory(Long CategoryId, CategoryRequestDTO categoryRequestDTO);
    CategoryResponseDTO deleteCategory(Long categoryId);
}
