package com.bitehub.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private static final String SRC_UPLOAD_DIR = "src/main/resources/static/images/uploads";
    private static final String TARGET_UPLOAD_DIR = "target/classes/static/images/uploads";

    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            Map<String, String> err = new HashMap<>();
            err.put("message", "Please select a file to upload");
            return ResponseEntity.badRequest().body(err);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            Map<String, String> err = new HashMap<>();
            err.put("message", "Only image files (JPEG, PNG, WebP, GIF) are allowed");
            return ResponseEntity.badRequest().body(err);
        }

        try {
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg");
            originalFilename = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
            String filename = UUID.randomUUID().toString().substring(0, 10) + "_" + originalFilename;

            File srcDir = new File(SRC_UPLOAD_DIR);
            if (!srcDir.exists()) srcDir.mkdirs();

            File targetDir = new File(TARGET_UPLOAD_DIR);
            if (!targetDir.exists()) targetDir.mkdirs();

            Path srcPath = Paths.get(SRC_UPLOAD_DIR, filename);
            Files.copy(file.getInputStream(), srcPath, StandardCopyOption.REPLACE_EXISTING);

            // Validate actual image content
            try {
                BufferedImage image = ImageIO.read(srcPath.toFile());
                if (image == null) {
                    Files.deleteIfExists(srcPath);
                    Map<String, String> err = new HashMap<>();
                    err.put("message", "Invalid image content");
                    return ResponseEntity.badRequest().body(err);
                }
            } catch (Exception e) {
                Files.deleteIfExists(srcPath);
                Map<String, String> err = new HashMap<>();
                err.put("message", "Failed to parse image content");
                return ResponseEntity.badRequest().body(err);
            }

            try {
                Path targetPath = Paths.get(TARGET_UPLOAD_DIR, filename);
                Files.copy(srcPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception ignored) {}

            Map<String, String> res = new HashMap<>();
            res.put("url", "/images/uploads/" + filename);
            res.put("filename", filename);
            return ResponseEntity.ok(res);

        } catch (IOException e) {
            Map<String, String> err = new HashMap<>();
            err.put("message", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }
}