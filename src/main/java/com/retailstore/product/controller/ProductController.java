package com.retailstore.product.controller;

import com.retailstore.exception.ResourceNotFoundException;
import com.retailstore.product.dto.ProductRequestDTO;
import com.retailstore.product.dto.ProductResponseDTO;
import com.retailstore.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for managing products in the retail store.
 * <p>
 * Provides endpoints for creating, reading, updating, and deleting products.
 * Stock/inventory management is handled separately by InventoryService.
 * </p>
 */
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Creates a new product.
     *
     * @param productRequestDTO DTO containing product metadata (name, description, price, categoryId)
     * @return ResponseEntity with {@link ProductResponseDTO} of the created product and HTTP status 201 (Created)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO productRequestDTO){

        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(productRequestDTO));
    }

    /**
     * Retrieves all products.
     *
     * @return ResponseEntity with a list of {@link ProductResponseDTO} and HTTP status 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts(){

        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * Retrieves a single product by ID.
     *
     * @param productId ID of the product to retrieve
     * @return ResponseEntity with {@link ProductResponseDTO} and HTTP status 200 (OK)
     * @throws ResourceNotFoundException if the product with the given ID does not exist
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long productId){

        return ResponseEntity.ok(productService.getProductById(productId));
    }

    /**
     * Updates an existing product.
     *
     * @param productId         ID of the product to update
     * @param productRequestDTO DTO containing updated product metadata (name, description, price, categoryId)
     * @return ResponseEntity with {@link ProductResponseDTO} of the updated product and HTTP status 200 (OK)
     * @throws ResourceNotFoundException if the product or the category does not exist
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long productId,
                                                            @Valid @RequestBody ProductRequestDTO productRequestDTO){

        return ResponseEntity.ok(productService.updateProduct(productId, productRequestDTO));
    }

    /**
     * Deletes a product by ID.
     *
     * @param productId ID of the product to delete
     * @return ResponseEntity with {@link ProductResponseDTO} of the deleted product and HTTP status 200 (OK)
     * @throws ResourceNotFoundException if the product with the given ID does not exist
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> deleteProduct(@PathVariable Long productId){

        return ResponseEntity.ok(productService.deleteProduct(productId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/{productId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponseDTO> uploadProductImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file) {

        return ResponseEntity.ok(productService.saveProductImage(productId, file));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByCategory(
            @PathVariable Long categoryId) {

        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }
}
