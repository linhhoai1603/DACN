package com.example.documentmanagementbackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Bảng chính: document_metadata.
 * Luôn chứa thông tin của version MỚI NHẤT.
 * Mỗi file (theo tên) chỉ có 1 record ở đây.
 */
@Entity
@Table(name = "document_metadata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "public_id", nullable = false)
    private String publicId;

    @Column(name = "url", nullable = false, length = 1000)
    private String url;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "uploaded_by")
    private String uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    // Mô tả thay đổi của version hiện tại
    @Column(name = "commit_message")
    private String commitMessage;

    // Version hiện tại, dạng string "v1", "v2", ...
    @Column(name = "version", nullable = false)
    private String version;

    // Thời điểm tạo lần đầu (không thay đổi khi update)
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // FK trỏ tới bản ghi version hiện tại trong document_versions
    @Column(name = "current_version_id")
    private Long currentVersionId;
}
