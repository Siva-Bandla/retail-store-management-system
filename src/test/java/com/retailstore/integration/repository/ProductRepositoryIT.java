package com.retailstore.integration.repository;

import com.retailstore.product.entity.Product;
import com.retailstore.product.repository.ProductRepository;
import com.retailstore.testdata.ProductTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(ProductTestData.class)
class ProductRepositoryIT {

    @Autowired private ProductRepository productRepository;
    @Autowired private ProductTestData productTestData;

    // ==================<< FIND ALL BY DELETED = FALSE >>=========================
    @Test
    @DisplayName("findAllByDeletedFalse returns only active products")
    void findAllByDeletedFalse_returnsActive() {

        Product p1 = productTestData.createProduct(1L, 999.0);
        Product p2 = productTestData.createProduct(2L, 1999.0);

        Product deleted = productTestData.createProduct(3L, 500.0);
        deleted.setDeleted(true);
        productRepository.save(deleted);

        List<Product> list = productRepository.findAllByDeletedFalse();

        assertThat(list)
                .hasSize(2)
                .extracting(Product::getId)
                .containsExactlyInAnyOrder(p1.getId(), p2.getId());
    }

    // ===================<< FIND BY ID AND DELETED = FALSE >>=====================
    @Test
    @DisplayName("findByIdAndDeletedFalse returns product when active")
    void findByIdAndDeletedFalse_active() {

        Product product = productTestData.createProduct(10L, 1500.0);

        Optional<Product> found = productRepository.findByIdAndDeletedFalse(product.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getCategoryId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("findByIdAndDeletedFalse returns empty when deleted")
    void findByIdAndDeletedFalse_deleted() {

        Product product = productTestData.createProduct(20L, 2500.0);
        product.setDeleted(true);
        productRepository.save(product);

        Optional<Product> found = productRepository.findByIdAndDeletedFalse(product.getId());

        assertThat(found).isEmpty();
    }

    // ===================<< EXISTS BY ID AND DELETED = FALSE >>=====================
    @Test
    @DisplayName("existsByIdAndDeletedFalse returns true for active product")
    void existsByIdAndDeletedFalse_true() {

        Product product = productTestData.createProduct(30L, 1200.0);

        boolean exists = productRepository.existsByIdAndDeletedFalse(product.getId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByIdAndDeletedFalse returns false for deleted product")
    void existsByIdAndDeletedFalse_false() {

        Product product = productTestData.createProduct(40L, 3000.0);
        product.setDeleted(true);
        productRepository.save(product);

        boolean exists = productRepository.existsByIdAndDeletedFalse(product.getId());

        assertThat(exists).isFalse();
    }

    // ===================<< EXISTS BY CATEGORY AND DELETED = FALSE >>=====================
    @Test
    @DisplayName("existsByCategoryIdAndDeletedFalse returns true for active product")
    void existsByCategoryIdAndDeletedFalse_true() {

        productTestData.createProduct(7L, 999.0);

        boolean exists = productRepository.existsByCategoryIdAndDeletedFalse(7L);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByCategoryIdAndDeletedFalse returns false when all deleted")
    void existsByCategoryIdAndDeletedFalse_false() {

        Product product = productTestData.createProduct(8L, 1999.0);
        product.setDeleted(true);
        productRepository.save(product);

        boolean exists = productRepository.existsByCategoryIdAndDeletedFalse(8L);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByCategoryIdAndDeletedFalse returns false when none exist")
    void existsByCategoryIdAndDeletedFalse_none() {

        boolean exists = productRepository.existsByCategoryIdAndDeletedFalse(999L);

        assertThat(exists).isFalse();
    }
}