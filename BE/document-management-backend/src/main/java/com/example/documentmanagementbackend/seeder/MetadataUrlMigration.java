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
        // Migration disabled — URL/publicId are now stored correctly at upload time.
        // Cloudinary secure_url already contains the extension in the public_id path.
        // Re-enabling this would corrupt URLs by double-appending extensions.
        System.out.println("[Migration] MetadataUrlMigration skipped (disabled).");

        // One-time cleanup: fix records that were corrupted by previous migration runs
        // (double extension like .pdf.pdf or .docx.docx)
        fixDoubleExtensions();
    }

    private void fixDoubleExtensions() {
        List<DocumentMetadata> all = metadataRepository.findAll();
        boolean anyFixed = false;

        for (DocumentMetadata doc : all) {
            String ext = getExtension(doc.getFileName());
            if (ext.isEmpty()) continue;

            String doubleExt = "." + ext + "." + ext;
            boolean changed = false;

            if (doc.getUrl() != null && doc.getUrl().endsWith(doubleExt)) {
                doc.setUrl(doc.getUrl().substring(0, doc.getUrl().length() - ext.length() - 1));
                changed = true;
            }

            if (doc.getPublicId() != null && doc.getPublicId().endsWith(doubleExt)) {
                doc.setPublicId(doc.getPublicId().substring(0, doc.getPublicId().length() - ext.length() - 1));
                changed = true;
            }

            if (changed) {
                metadataRepository.save(doc);
                anyFixed = true;
                System.out.println("[Migration] Cleaned double-extension: " + doc.getFileName() + " → " + doc.getUrl());
            }
        }

        if (!anyFixed) {
            System.out.println("[Migration] No double-extension records found.");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
