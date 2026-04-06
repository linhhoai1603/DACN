package com.example.documentmanagementbackend.seeder;

import com.example.documentmanagementbackend.model.DocumentMetadata;
import com.example.documentmanagementbackend.repository.DocumentMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * One-time migration: fix existing records that were uploaded without extension in URL/publicId.
 * Runs at startup, idempotent (skips records already having extension).
 */
@Component
@Order(2)
@RequiredArgsConstructor
public class MetadataUrlMigration implements CommandLineRunner {

    private final DocumentMetadataRepository metadataRepository;

    @Override
    public void run(String... args) {
        List<DocumentMetadata> all = metadataRepository.findAll();
        boolean anyUpdated = false;

        for (DocumentMetadata doc : all) {
            String ext = getExtension(doc.getFileName());
            if (ext.isEmpty()) continue;

            boolean urlFixed = false;
            boolean publicIdFixed = false;

            // Fix URL: nếu URL không kết thúc bằng extension
            if (doc.getUrl() != null && !doc.getUrl().endsWith("." + ext)) {
                doc.setUrl(doc.getUrl() + "." + ext);
                urlFixed = true;
            }

            // Fix publicId: nếu publicId không kết thúc bằng extension
            if (doc.getPublicId() != null && !doc.getPublicId().endsWith("." + ext)) {
                doc.setPublicId(doc.getPublicId() + "." + ext);
                publicIdFixed = true;
            }

            if (urlFixed || publicIdFixed) {
                metadataRepository.save(doc);
                anyUpdated = true;
                System.out.println("[Migration] Fixed: " + doc.getFileName() + " → " + doc.getUrl());
            }
        }

        if (!anyUpdated) {
            System.out.println("[Migration] All records already have correct extensions. Skipping.");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
