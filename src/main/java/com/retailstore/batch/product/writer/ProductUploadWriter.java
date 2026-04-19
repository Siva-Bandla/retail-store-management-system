package com.retailstore.batch.product.writer;

import com.retailstore.batch.product.model.ProductUploadDTO;
import com.retailstore.batch.product.util.ProductCSVGenerator;
import com.retailstore.batch.util.EmailSender;
import com.retailstore.category.entity.Category;
import com.retailstore.category.repository.CategoryRepository;
import com.retailstore.exception.ResourceNotFoundException;
import com.retailstore.inventory.entity.Inventory;
import com.retailstore.inventory.repository.InventoryRepository;
import com.retailstore.product.entity.Product;
import com.retailstore.product.repository.ProductRepository;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProductUploadWriter implements ItemWriter<ProductUploadDTO>, StepExecutionListener {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final CategoryRepository categoryRepository;

    private final ProductCSVGenerator csvGenerator;
    private final EmailSender emailSender;

    private final List<ProductUploadDTO> allProducts = new ArrayList<>();
    private final Map<Long, Boolean> categoryCache = new HashMap<>();

    public ProductUploadWriter(ProductRepository productRepository, InventoryRepository inventoryRepository,
                               CategoryRepository categoryRepository,
                               ProductCSVGenerator csvGenerator, EmailSender emailSender) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.categoryRepository = categoryRepository;
        this.csvGenerator = csvGenerator;
        this.emailSender = emailSender;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        // Clear previous run state so report only contains current batch
        allProducts.clear();

        // Cache categories once per step to avoid repeated category existence queries
        categoryCache.clear();
        List<Category> categories = categoryRepository.findAll();
        for (Category category : categories) {
            categoryCache.put(category.getId(), true);
        }
    }

    @Override
    @Transactional
    public void write(Chunk<? extends ProductUploadDTO> chunk) throws Exception {

        List<Long> productIds = chunk.getItems().stream()
                .map(ProductUploadDTO::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Product> existingProducts = new HashMap<>();
        if (!productIds.isEmpty()) {
            productRepository.findAllById(productIds)
                    .forEach(product -> existingProducts.put(product.getId(), product));
        }

        for (ProductUploadDTO dto: chunk.getItems()){

            //validate category
            if (!categoryRepository.existsById(dto.getCategoryId())){
                throw new ResourceNotFoundException("Category not found: " + dto.getCategoryId());
            }

            Product product;
            boolean isUpdate = false;

            //update existing only of id provided
            if (dto.getId() != null) {
                product = existingProducts.get(dto.getId());
                if (product != null) {
                    isUpdate = true;
                } else {
                    product = new Product();
                }
            } else {
                product = new Product();
            }

            product.setCategoryId(dto.getCategoryId());
            product.setName(dto.getName());
            product.setDescription(dto.getDescription());
            product.setPrice(dto.getPrice());
            product.setDeleted(false);
            product.setImageUrl(dto.getImageUrl());

            Product savedProduct = productRepository.save(product);

            //Inventory update
            Inventory inventory = inventoryRepository.findByProductId(savedProduct.getId())
                    .orElseGet(() -> {
                        Inventory inv = new Inventory();
                        inv.setProductId(savedProduct.getId());
                        inv.setStock(0);
                        return inv;
                    });

            inventory.setStock(inventory.getStock() + dto.getQuantity());
            inventory.setDeleted(false);
            inventoryRepository.save(inventory);

            dto.setId(savedProduct.getId());
            dto.setStatus(isUpdate ? "Updated" : "Created");

            allProducts.add(dto);
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        try {
            File csv = csvGenerator.generateCSV(allProducts);
            emailSender.sendReport("Products Uploaded Report",
                    "Please find attached product upload report.", csv);
            return ExitStatus.COMPLETED;

        }catch (Exception exception) {
            exception.printStackTrace();
            return ExitStatus.FAILED;
        }
    }
}
