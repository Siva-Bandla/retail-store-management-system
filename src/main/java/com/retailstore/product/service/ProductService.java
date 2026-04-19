package com.retailstore.product.service;

import com.retailstore.product.dto.ProductRequestDTO;
import com.retailstore.product.dto.ProductResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO);
    List<ProductResponseDTO> getAllProducts();
    ProductResponseDTO getProductById(Long productId);
    ProductResponseDTO updateProduct(Long productId, ProductRequestDTO productRequestDTO);
    ProductResponseDTO deleteProduct(Long productId);

    ProductResponseDTO saveProductImage(Long productId, MultipartFile file);

    List<ProductResponseDTO> getProductsByCategory(Long categoryId);
}
