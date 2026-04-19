package com.retailstore.batch.controller;

import com.retailstore.batch.service.BatchJobFileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/batch")
@Slf4j
public class BatchJobFileUploadController {

    @Autowired
    private BatchJobFileUploadService uploadService;

    @PostMapping("/upload/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadProductsFile(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("Received product file upload request");
        return uploadService.uploadProductFile(file);
    }

    @PostMapping("/upload/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadStockFile(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("Received stock reconciliation file upload request");
        return uploadService.uploadStockFile(file);
    }
}
