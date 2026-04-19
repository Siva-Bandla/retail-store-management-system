package com.retailstore.product.repository;

import com.retailstore.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByDeletedFalse();

    Optional<Product> findByIdAndDeletedFalse(Long productId);

    boolean existsByIdAndDeletedFalse(Long productId);

    boolean existsByCategoryIdAndDeletedFalse(Long categoryId);

    List<Product> findByCategoryId(Long categoryId);
}
