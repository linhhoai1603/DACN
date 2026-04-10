package com.example.documentmanagementbackend.service;

import com.example.documentmanagementbackend.dto.response.DocumentVersionResponse;
import com.example.documentmanagementbackend.model.DocumentVersion;
import com.example.documentmanagementbackend.repository.DocumentMetadataRepository;
import com.example.documentmanagementbackend.repository.DocumentVersionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentVersionService {

    private final DocumentVersionRepository versionRepository;
    private final DocumentMetadataRepository metadataRepository;

    public DocumentVersionService(DocumentVersionRepository versionRepository,
                                  DocumentMetadataRepository metadataRepository) {
        this.versionRepository = versionRepository;
        this.metadataRepository = metadataRepository;
    }

    /**
     * Lấy toàn bộ version history của một document theo documentId.
     * Trả về danh sách sắp xếp mới nhất trước (DESC).
     */
    public List<DocumentVersionResponse> getVersionsByDocumentId(Long documentId) {
        // Kiểm tra document tồn tại
        metadataRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));

        List<DocumentVersion> versions =
                versionRepository.findByDocumentIdOrderByVersionNumberDesc(documentId);

        return versions.stream()
                .map(v -> new DocumentVersionResponse(
                        v.getId(),
                        v.getVersionNumber(),
                        "v" + v.getVersionNumber(),
                        v.getFileName(),
                        v.getFileUrl(),
                        v.getFileSize(),
                        v.getUploadedBy(),
                        v.getUploadedAt(),
                        v.getCommitMessage(),
                        v.getIsLatest()
                ))
                .toList();
    }
}
