package com.retailstore.product.service;

import com.retailstore.category.repository.CategoryRepository;
import com.retailstore.exception.ResourceNotFoundException;
import com.retailstore.inventory.entity.Inventory;
import com.retailstore.inventory.repository.InventoryRepository;
import com.retailstore.product.dto.ProductRequestDTO;
import com.retailstore.product.dto.ProductResponseDTO;
import com.retailstore.product.entity.Product;
import com.retailstore.product.mapper.ProductMapper;
import com.retailstore.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service implementation responsible for handling business operations
 * related to {@link Product} management.
 *
 * <p>This service provides functionality for:</p>
 * <ul>
 *     <li>Creating new products</li>
 *     <li>Retrieving product details</li>
 *     <li>Fetching all available products</li>
 *     <li>Updating existing product information</li>
 *     <li>Soft deleting products</li>
 * </ul>
 *
 * <p>Order creation includes validation of user existence, product availability,
 * inventory checks, and calculation of the total order amount.</p>
 */
@Service
public class ProductServiceImpl implements ProductService{

    @Value("${app.base-url}")
    private String baseUrl;
    private static final Path IMAGE_UPLOAD_DIR = Paths.get("uploads", "images");

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              InventoryRepository inventoryRepository){
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Creates a new product in the system.
     * <p>
     * This method performs the following steps:
     * <ol>
     *     <li>Validates that the category specified in {@code productRequestDTO} exists.</li>
     *     <li>Creates a new {@link Product} entity and sets its name, description, price, and category.</li>
     *     <li>Saves the product to the database.</li>
     *     <li>Maps the saved product to a {@link ProductResponseDTO},
     *     including stock (defaults to 0 if inventory does not exist), and returns it.</li>
     * </ol>
     * <p>
     * Note: Inventory creation and stock management are handled separately by the InventoryService.
     *
     * @param productRequestDTO DTO containing product details such as name, description, price, and categoryId.
     * @return {@link ProductResponseDTO} containing the saved product information,
     * including id, name, description, price, categoryId, and stock.
     * @throws ResourceNotFoundException if the category with the specified ID does not exist.
     */
    @Override
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO) {

        //Validate category
        if (!categoryRepository.existsById(productRequestDTO.getCategoryId())){
            throw new ResourceNotFoundException(
                    "Category not found with id: " + productRequestDTO.getCategoryId()
            );
        }

        //Create product
        Product product = new Product();
        product.setName(productRequestDTO.getName());
        product.setDescription(productRequestDTO.getDescription());
        product.setCategoryId(productRequestDTO.getCategoryId());
        product.setPrice(productRequestDTO.getPrice());

        Product savedProduct = productRepository.save(product);

        //Fetch or create inventory
        Inventory inventory = inventoryRepository.findByProductId(savedProduct.getId())
                .orElseGet(() -> {
                    Inventory newInventory = new Inventory();
                    newInventory.setProductId(savedProduct.getId());
                    newInventory.setStock(0);
                    return newInventory;
                });

        //Update stock
        inventory.setStock(inventory.getStock() +
                (productRequestDTO.getQuantity() == null ? 0 : productRequestDTO.getQuantity()));
        inventoryRepository.save(inventory);

        //Map to DTO and return
        return ProductMapper.mapToProductResponseDTO(savedProduct, inventory.getStock());
    }


    /**
     * Retrieves all products from the database and converts them into a list of
     * ProductResponseDTO objects for client responses.
     *
     * <p>For each Product entity:
     * <ul>
     *     <li>The Product entity is fetched from the repository.</li>
     *     <li>The associated Inventory entity is passed to the ProductMapper
     *         to map the product and its stock into a ProductResponseDTO.</li>
     * </ul>
     *
     * @return a {@link List} of {@link ProductResponseDTO} containing
     *         the essential details of all products, including id, name,
     *         description, price, stock, and categoryId
     */
    @Override
    public List<ProductResponseDTO> getAllProducts() {

        List<Product> products = productRepository.findAll();

        // Batch fetch inventory to avoid N+1 problem
        List<Long> productIds = products.stream()
                .map(Product::getId)
                .toList();
        List<Inventory> inventories = inventoryRepository.findByProductIdInAndDeletedFalse(productIds);

        Map<Long, Integer> stockMap = inventories.stream()
                .collect(Collectors.toMap(
                        Inventory::getProductId,
                        Inventory::getStock
                ));

        return products.stream()
                .map(product -> ProductMapper.mapToProductResponseDTO(
                        product, stockMap.getOrDefault(product.getId(), 0)))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single product by its ID and maps it to a {@link ProductResponseDTO}.
     *
     * <p>This method performs the following steps:
     * <ol>
     *     <li>Fetches the {@link Product} entity from the repository by the given {@code productId}.</li>
     *     <li>If the product does not exist, throws a {@link ResourceNotFoundException}.</li>
     *     <li>Maps the product and its associated {@link Inventory} to a {@link ProductResponseDTO}.</li>
     * </ol>
     *
     * @param productId the ID of the product to retrieve.
     * @return {@link ProductResponseDTO} containing the product information,
     * including id, name, description, price, stock, and categoryId.
     * @throws ResourceNotFoundException if no product is found with the specified {@code productId}.
     */
    @Override
    public ProductResponseDTO getProductById(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with product id: " + productId
                ));

        return ProductMapper.mapToProductResponseDTO(product, fetchStock(productId));
    }

    /**
     * Updates an existing product's metadata in the system.
     * <p>
     * This method allows updating the product's name, description, price, and category.
     * Inventory/stock updates are not handled here and should be managed separately by the InventoryService.
     * </p>
     *
     * @param productId         The ID of the product to update.
     * @param productRequestDTO DTO containing the updated product details: name, description, price, and categoryId.
     * @return {@link ProductResponseDTO} containing the updated product information, including stock if inventory exists.
     * @throws ResourceNotFoundException if the product with the given ID does not exist,
     *                                   or if the category with the given categoryId does not exist.
     */
    @Override
    @Transactional
    public ProductResponseDTO updateProduct(Long productId, ProductRequestDTO productRequestDTO) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with product id: " + productId
                ));

        if (!categoryRepository.existsById(productRequestDTO.getCategoryId())){
            throw new ResourceNotFoundException(
                    "Category not found with id: " + productRequestDTO.getCategoryId()
            );
        }

        product.setName(productRequestDTO.getName());
        product.setDescription(productRequestDTO.getDescription());
        product.setCategoryId(productRequestDTO.getCategoryId());
        product.setPrice(productRequestDTO.getPrice());

        if (productRequestDTO.getDeleted() != null) {
            product.setDeleted(productRequestDTO.getDeleted());
        }

        Product savedProduct = productRepository.save(product);

        //Fetch or create inventory
        Inventory inventory = inventoryRepository.findByProductId(savedProduct.getId())
                .orElseGet(() -> {
                    Inventory newInventory = new Inventory();
                    newInventory.setProductId(savedProduct.getId());
                    newInventory.setStock(0);
                    return newInventory;
                });

        //Update stock
        inventory.setStock(inventory.getStock() +
                (productRequestDTO.getQuantity() == null ? 0 : productRequestDTO.getQuantity()));
        inventoryRepository.save(inventory);

        return ProductMapper.mapToProductResponseDTO(savedProduct, fetchStock(productId));
    }

    /**
     * Deletes an existing product from the system by its ID.
     * <p>
     * This method first fetches the product to build a {@link ProductResponseDTO} containing its details,
     * then deletes the product entity. Inventory deletion (if mapped with cascade) will be handled automatically by JPA.
     * </p>
     *
     * @param productId The ID of the product to delete.
     * @return {@link ProductResponseDTO} containing the details of the deleted product,
     * including stock if inventory existed.
     * @throws ResourceNotFoundException if the product with the given ID does not exist.
     */
    @Override
    @Transactional
    public ProductResponseDTO deleteProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with product id: " + productId
                ));

        ProductResponseDTO productResponseDTO =
                ProductMapper.mapToProductResponseDTO(product, fetchStock(productId));

        product.setDeleted(true);
        productRepository.save(product);

        return productResponseDTO;
    }

    @Override
    @Transactional
    public ProductResponseDTO saveProductImage(Long productId, MultipartFile file) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with product id: " + productId
                ));

        try {
            Files.createDirectories(IMAGE_UPLOAD_DIR);

            // DELETE OLD IMAGE
            if (product.getImageUrl() != null) {
                try {
                    String oldImageName = product.getImageUrl().substring(
                            product.getImageUrl().lastIndexOf("/") + 1
                    );

                    Path oldImagePath = IMAGE_UPLOAD_DIR.resolve(oldImageName);
                    Files.deleteIfExists(oldImagePath);

                } catch (Exception e) {
                    System.out.println("Failed to delete old image: " + e.getMessage());
                }
            }

            String extension = "";
            String originalName = file.getOriginalFilename();
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            String filename = "product-" + productId + "-" + System.currentTimeMillis() + extension;
            Path targetPath = IMAGE_UPLOAD_DIR.resolve(filename);
            Files.copy(file.getInputStream(), targetPath);

            product.setImageUrl(baseUrl + "/images/" + filename);
            productRepository.save(product);

            return ProductMapper.mapToProductResponseDTO(product, fetchStock(productId));
        } catch (IOException e) {
            throw new RuntimeException("Failed to store product image", e);
        }
    }

    @Override
    public List<ProductResponseDTO> getProductsByCategory(Long categoryId) {

        // Validate category exists
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException(
                    "Category not found with id: " + categoryId
            );
        }

        List<Product> products =
                productRepository.findByCategoryId(categoryId);

        // Batch fetch inventory
        List<Long> productIds = products.stream()
                .map(Product::getId)
                .toList();

        List<Inventory> inventories =
                inventoryRepository.findByProductIdInAndDeletedFalse(productIds);

        Map<Long, Integer> stockMap = inventories.stream()
                .collect(Collectors.toMap(
                        Inventory::getProductId,
                        Inventory::getStock
                ));

        return products.stream()
                .map(product -> ProductMapper.mapToProductResponseDTO(
                        product,
                        stockMap.getOrDefault(product.getId(), 0)
                ))
                .collect(Collectors.toList());
    }

    private int fetchStock(Long productId){

        return inventoryRepository.findByProductIdAndDeletedFalse(productId)
                .map(Inventory::getStock)
                .orElse(0);
    }
}
