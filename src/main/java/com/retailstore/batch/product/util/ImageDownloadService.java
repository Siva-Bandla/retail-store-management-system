package com.retailstore.batch.product.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class ImageDownloadService {

    private static final String IMAGES_DIR = "uploads/images/products/";
    private static final String PLACEHOLDER_IMAGE = "/images/placeholder.png";
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int TIMEOUT_MS = 10000; // 10 seconds
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

    public String downloadImage(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            log.debug("No image URL provided, using placeholder");
            return PLACEHOLDER_IMAGE;
        }

        HttpURLConnection connection = null;
        try {
            // Create images directory if it doesn't exist
            Path imagesPath = Paths.get(IMAGES_DIR);
            Files.createDirectories(imagesPath);

            // Open connection
            connection = (HttpURLConnection) new URL(imageUrl).openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept", "image/*, */*");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setInstanceFollowRedirects(true);

            // Check response code
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                log.warn("Failed to download image: HTTP {} for {}", responseCode, imageUrl);
                return PLACEHOLDER_IMAGE;
            }

            // Validate content type
            String contentType = connection.getContentType();
            if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
                log.warn("Invalid content type: {} for {}", contentType, imageUrl);
                return PLACEHOLDER_IMAGE;
            }

            // Check content length
            long contentLength = connection.getContentLengthLong();
            if (contentLength > MAX_IMAGE_SIZE) {
                log.warn("Image too large: {} bytes for {}", contentLength, imageUrl);
                return PLACEHOLDER_IMAGE;
            }

            // Generate unique filename
            String fileExtension = getFileExtension(imageUrl, contentType);
            String fileName = UUID.randomUUID().toString() + "." + fileExtension;
            Path targetPath = imagesPath.resolve(fileName);

            // Download the image with size validation
            try (InputStream in = connection.getInputStream()) {
                long bytesCopied = Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);

                // Validate downloaded file size
                if (bytesCopied == 0) {
                    Files.deleteIfExists(targetPath);
                    log.warn("Downloaded file is empty for {}", imageUrl);
                    return PLACEHOLDER_IMAGE;
                }

                if (bytesCopied > MAX_IMAGE_SIZE) {
                    Files.deleteIfExists(targetPath);
                    log.warn("Downloaded image exceeds max size: {} bytes for {}", bytesCopied, imageUrl);
                    return PLACEHOLDER_IMAGE;
                }

                // Validate it's actually an image using file magic bytes
                if (!isValidImageFile(targetPath)) {
                    Files.deleteIfExists(targetPath);
                    log.warn("File content is not a valid image for {}", imageUrl);
                    return PLACEHOLDER_IMAGE;
                }

                String relativePath = "/images/products/" + fileName;
                log.info("Successfully downloaded and stored image: {} -> {} ({} bytes)",
                        imageUrl, relativePath, bytesCopied);
                return relativePath;
            }

        } catch (IOException e) {
            log.warn("Failed to download image from {}: {}", imageUrl, e.getMessage());
            return PLACEHOLDER_IMAGE;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String getFileExtension(String url, String contentType) {
        // Try to get extension from URL first
        try {
            String path = new URL(url).getPath();
            int lastDot = path.lastIndexOf('.');
            if (lastDot > 0 && lastDot < path.length() - 1) {
                String ext = path.substring(lastDot + 1).toLowerCase();
                // Remove query parameters
                int questionMark = ext.indexOf('?');
                if (questionMark > 0) {
                    ext = ext.substring(0, questionMark);
                }
                // Validate extension
                if (ext.matches("jpg|jpeg|png|gif|webp|bmp")) {
                    return ext.equals("jpeg") ? "jpg" : ext;
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract extension from URL: {}", url);
        }

        // Fall back to content-type
        if (contentType != null) {
            String type = contentType.toLowerCase();
            if (type.contains("jpeg")) return "jpg";
            if (type.contains("png")) return "png";
            if (type.contains("gif")) return "gif";
            if (type.contains("webp")) return "webp";
            if (type.contains("bmp")) return "bmp";
        }

        return "jpg"; // Default extension
    }

    private boolean isValidImageFile(Path filePath) {
        try {
            byte[] magic = new byte[12];
            int bytesRead = 0;

            try (InputStream in = Files.newInputStream(filePath)) {
                bytesRead = in.read(magic);
            }

            if (bytesRead < 2) {
                return false;
            }

            // Check for common image file signatures (magic bytes)
            return isJPEG(magic) || isPNG(magic) || isGIF(magic) || isWebP(magic) || isBMP(magic);
        } catch (Exception e) {
            log.debug("Could not validate image file: {}", e.getMessage());
            return false;
        }
    }
    private boolean isJPEG(byte[] magic) {
        return magic.length >= 2 && magic[0] == (byte) 0xFF && magic[1] == (byte) 0xD8;
    }

    private boolean isPNG(byte[] magic) {
        return magic.length >= 8 &&
                magic[0] == (byte) 0x89 && magic[1] == 0x50 &&
                magic[2] == 0x4E && magic[3] == 0x47;
    }

    private boolean isGIF(byte[] magic) {
        return magic.length >= 6 &&
                (magic[0] == 0x47 && magic[1] == 0x49 && magic[2] == 0x46); // "GIF"
    }

    private boolean isWebP(byte[] magic) {
        return magic.length >= 12 &&
                magic[0] == 0x52 && magic[1] == 0x49 &&
                magic[2] == 0x46 && magic[3] == 0x46 && // "RIFF"
                magic[8] == 0x57 && magic[9] == 0x45 &&
                magic[10] == 0x42 && magic[11] == 0x50; // "WEBP"
    }

    private boolean isBMP(byte[] magic) {
        return magic.length >= 2 && magic[0] == 0x42 && magic[1] == 0x4D; // "BM"
    }
}