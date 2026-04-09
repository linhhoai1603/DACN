package com.example.documentmanagementbackend.repository;

import com.example.documentmanagementbackend.dto.response.DocumentMetadataNativeResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.documentmanagementbackend.model.DocumentMetadata;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentMetadataRepository extends JpaRepository<DocumentMetadata, Long> {
    List<DocumentMetadata> findByFileNameOrderByUploadedAtDesc(String fileName);
    Optional<DocumentMetadata> findByFileName(String fileName);

    @Query(value = """
            SELECT
                dm.id AS id,
                dm.file_name AS fileName,
                dm.public_id AS publicId,
                dm.url AS url,
                dm.file_size AS fileSize,
                dm.uploaded_by AS uploadedBy,
                dm.uploaded_at AS uploadedAt,
                dm.commit_message AS commitMessage,
                dm.version AS version
            FROM document_metadata dm
            INNER JOIN document_versions dv ON dm.current_version_id = dv.id
            WHERE dv.is_latest = true
            ORDER BY dv.uploaded_at DESC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<DocumentMetadataNativeResponse> findLatestDocumentMetadataByLimitOffset(
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    default List<DocumentMetadataNativeResponse> findLatestDocumentMetadata(int page, int index) {
        int normalizedPageSize = Math.max(page, 1);
        int normalizedPageIndex = Math.max(index, 0);
        int offset = normalizedPageIndex * normalizedPageSize;
        return findLatestDocumentMetadataByLimitOffset(normalizedPageSize, offset);
    }
}
