package com.retailstore.testdata;

import com.retailstore.product.entity.Product;
import com.retailstore.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

public class ProductTestData {

    @Autowired
    private ProductRepository productRepository;

    public Product createProduct(Long categoryId, double price) {
        Product product = new Product();
        product.setName("Test Product " + System.nanoTime());
        product.setDescription("Sample product");
        product.setPrice(BigDecimal.valueOf(price));
        product.setCategoryId(categoryId);

        return productRepository.save(product);
    }
}