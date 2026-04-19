package com.retailstore.common.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfig {

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get("uploads", "images"));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }
}