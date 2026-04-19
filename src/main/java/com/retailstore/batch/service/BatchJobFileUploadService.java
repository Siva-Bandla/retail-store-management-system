package com.retailstore.batch.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class BatchJobFileUploadService {

    @Value("${batch.upload.dir:uploads/}")
    private String uploadDir;

    public ResponseEntity<?> uploadProductFile(MultipartFile file) throws IOException {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            if (!file.getOriginalFilename().endsWith(".csv")) {
                return ResponseEntity.badRequest().body("Only CSV files are allowed");
            }

            String targetPath = uploadDir + "products-upload.csv";
            saveFile(file, targetPath);

            log.info("Product CSV uploaded successfully: {}", targetPath);
            return ResponseEntity.ok().body("Product file uploaded successfully. Batch job will run at 2 AM.");

        } catch (Exception e) {
            log.error("Error uploading product file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        }
    }

    public ResponseEntity<?> uploadStockFile(MultipartFile file) throws IOException {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            if (!file.getOriginalFilename().endsWith(".csv")) {
                return ResponseEntity.badRequest().body("Only CSV files are allowed");
            }

            String targetPath = uploadDir + "stocks-reconciliation.csv";
            saveFile(file, targetPath);

            log.info("Stock CSV uploaded successfully: {}", targetPath);
            return ResponseEntity.ok().body("Stock file uploaded successfully. Batch job will run at 2 AM.");

        } catch (Exception e) {
            log.error("Error uploading stock file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        }
    }

    private void saveFile(MultipartFile file, String targetPath) throws IOException {
        // Create directories if they don't exist
        Path path = Paths.get(targetPath);
        Files.createDirectories(path.getParent());

        // Save the file
        byte[] bytes = file.getBytes();
        Files.write(path, bytes);
    }
}
