package com.example.documentmanagementbackend.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * One-time migration: drop bảng document_versions nếu cột document_id là UUID
 * (tạo từ phiên bản cũ), để Hibernate tạo lại với kiểu BIGINT đúng.
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class DocumentVersionTableMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            // Kiểm tra kiểu dữ liệu của cột document_id
            String dataType = jdbcTemplate.queryForObject(
                    "SELECT data_type FROM information_schema.columns " +
                    "WHERE table_name = 'document_versions' AND column_name = 'document_id'",
                    String.class
            );

            if ("uuid".equalsIgnoreCase(dataType)) {
                System.out.println("[Migration] document_versions.document_id is UUID — dropping table to recreate with BIGINT...");
                jdbcTemplate.execute("DROP TABLE IF EXISTS document_versions");
                System.out.println("[Migration] Dropped document_versions. Hibernate will recreate it correctly.");
            } else {
                System.out.println("[Migration] document_versions.document_id type is '" + dataType + "' — no action needed.");
            }
        } catch (Exception e) {
            // Bảng chưa tồn tại → không cần làm gì
            System.out.println("[Migration] document_versions table not found or check failed: " + e.getMessage());
        }
    }
}
