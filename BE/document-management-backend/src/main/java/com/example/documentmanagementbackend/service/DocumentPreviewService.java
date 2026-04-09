package com.example.documentmanagementbackend.service;

import com.example.documentmanagementbackend.dto.response.DocumentMetadataResponse;
import com.example.documentmanagementbackend.model.DocumentMetadata;
import com.example.documentmanagementbackend.repository.DocumentMetadataRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DocumentPreviewService {

    private static final Set<String> OFFICE_TYPES = Set.of("doc", "docx", "xls", "xlsx");
    private static final String GOOGLE_VIEWER = "https://docs.google.com/viewer?embedded=true&url=";

    private final DocumentMetadataRepository metadataRepository;

    public DocumentPreviewService(DocumentMetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    public DocumentMetadataResponse getById(Long id) {
        DocumentMetadata doc = metadataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
        return toResponse(doc);
    }

    public List<DocumentMetadataResponse> getAll() {
        return metadataRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private DocumentMetadataResponse toResponse(DocumentMetadata doc) {
        String ext = getExtension(doc.getFileName());
        String previewUrl = buildPreviewUrl(ext, doc.getUrl(), doc.getId());

        return new DocumentMetadataResponse(
                doc.getId(),
                doc.getFileName(),
                doc.getPublicId(),
                doc.getUrl(),
                doc.getFileSize(),
                doc.getUploadedBy(),
                doc.getUploadedAt(),
                doc.getCommitMessage(),
                doc.getVersion(),   // đã là string "v1", "v2", ...
                ext,
                previewUrl
        );
    }

    private String buildPreviewUrl(String ext, String fileUrl, Long id) {
        // Dùng backend proxy /stream để đảm bảo đúng Content-Type
        // PDF: inline trong browser qua proxy
        // Office files: Google Docs Viewer trỏ đến proxy URL
        String streamUrl = "http://localhost:8080/files/" + id + "/stream";

        if ("pdf".equals(ext)) {
            return streamUrl;
        }
        if (OFFICE_TYPES.contains(ext)) {
            return GOOGLE_VIEWER + encodeUrl(streamUrl);
        }
        return streamUrl;
    }

    private String encodeUrl(String url) {
        try {
            return java.net.URLEncoder.encode(url, "UTF-8");
        } catch (Exception e) {
            return url;
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
