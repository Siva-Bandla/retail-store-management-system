package com.retailstore.testdata;

import com.retailstore.category.entity.Category;
import com.retailstore.category.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class CategoryTestData {

    @Autowired
    private CategoryRepository categoryRepository;

    public Category createCategory(String name){
        Category category = new Category();
        category.setName(name);

        return categoryRepository.save(category);
    }
}
