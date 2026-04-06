package com.example.documentmanagementbackend.seeder;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.documentmanagementbackend.model.DocumentMetadata;
import com.example.documentmanagementbackend.repository.DocumentMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Migration: đổi tất cả raw files trên Cloudinary từ authenticated → public
 * bằng cách dùng Admin API (explicit update).
 */
@Component
@Order(3)
@RequiredArgsConstructor
public class CloudinaryAccessModeMigration implements CommandLineRunner {

    private final DocumentMetadataRepository metadataRepository;
    private final Cloudinary cloudinary;

    @Override
    public void run(String... args) {
        // Migration disabled — files are uploaded as public (type=upload) already.
        // Running this on every startup wastes Cloudinary Admin API quota.
        System.out.println("[CloudinaryMigration] Skipped (disabled).");
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
