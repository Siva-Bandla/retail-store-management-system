package com.retailstore.integration.repository;

import com.retailstore.category.entity.Category;
import com.retailstore.category.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class CategoryRepositoryIT {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;


    @Test
    @DisplayName("Should save and retrieve category by ID")
    void testSaveAndFindById() {

        Category category = new Category();
        category.setName("Electronics");
        category.setDescription("Electronic items");

        Category saved = categoryRepository.save(category);

        Optional<Category> found = categoryRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Electronics");
    }


    @Test
    @DisplayName("Should return true when category exists by name")
    void testExistsByName_True() {

        Category category = new Category();
        category.setName("Fashion");
        categoryRepository.save(category);

        boolean exists = categoryRepository.existsByName("Fashion");

        assertThat(exists).isTrue();
    }


    @Test
    @DisplayName("Should return false when category does not exist by name")
    void testExistsByName_False() {

        boolean exists = categoryRepository.existsByName("NonExisting");

        assertThat(exists).isFalse();
    }


    @Test
    @DisplayName("Should retrieve all categories")
    void testFindAll() {

        categoryRepository.save(new Category(null, "Books", "All books"));
        categoryRepository.save(new Category(null, "Toys", "Kids toys"));

        List<Category> categories = categoryRepository.findAll();

        assertThat(categories).hasSize(2);
    }


    @Test
    @DisplayName("Should delete category successfully")
    void testDeleteCategory() {

        Category category = new Category();
        category.setName("Sports");
        Category saved = categoryRepository.save(category);

        categoryRepository.delete(saved);

        Optional<Category> found = categoryRepository.findById(saved.getId());

        assertThat(found).isEmpty();
    }


    @Test
    @DisplayName("Should enforce unique constraint on category name")
    void testUniqueNameConstraint() {

        Category c1 = new Category();
        c1.setName("Garden");
        categoryRepository.save(c1);

        // Flush to force SQL execution & constraint validation
        entityManager.flush();

        Category c2 = new Category();
        c2.setName("Garden");

        assertThatThrownBy(() -> {
            categoryRepository.save(c2);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }
}