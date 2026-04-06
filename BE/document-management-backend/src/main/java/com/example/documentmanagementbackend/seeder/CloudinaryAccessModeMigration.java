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
        List<DocumentMetadata> all = metadataRepository.findAll();

        for (DocumentMetadata doc : all) {
            String ext = getExtension(doc.getFileName());
            String publicId = doc.getPublicId();
            // Cloudinary API cần publicId không có extension
            if (!ext.isEmpty() && publicId.endsWith("." + ext)) {
                publicId = publicId.substring(0, publicId.length() - ext.length() - 1);
            }

            try {
                // Thử với publicId có extension trước
                String pidToUse = publicId + "." + ext;
                Map result;
                try {
                    result = cloudinary.api().update(pidToUse,
                            ObjectUtils.asMap("resource_type", "raw", "type", "upload", "access_mode", "public"));
                } catch (Exception e1) {
                    // Thử không có extension
                    result = cloudinary.api().update(publicId,
                            ObjectUtils.asMap("resource_type", "raw", "type", "upload", "access_mode", "public"));
                }
                System.out.println("[CloudinaryMigration] Updated access_mode=public: " + doc.getFileName()
                        + " → " + result.get("access_mode"));
            } catch (Exception e) {
                System.out.println("[CloudinaryMigration] Skipped: "
                        + doc.getFileName() + " — " + e.getMessage());
            }
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
