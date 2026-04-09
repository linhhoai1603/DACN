package com.example.documentmanagementbackend.service.impl;

import com.example.documentmanagementbackend.dto.response.DocumentMetadataNativeResponse;
import com.example.documentmanagementbackend.dto.response.DocumentMetadataResponse;
import com.example.documentmanagementbackend.repository.DocumentMetadataRepository;
import com.example.documentmanagementbackend.service.DocumentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private static final Set<String> OFFICE_TYPES = Set.of("doc", "docx", "xls", "xlsx");
    private static final String GOOGLE_VIEWER = "https://docs.google.com/viewer?embedded=true&url=";

    private final DocumentMetadataRepository metadataRepository;

    @Override
    public List<DocumentMetadataResponse> getDocuments(int index, int page) {
        return metadataRepository.findLatestDocumentMetadata(page, index)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public long countDocuments() {
        return metadataRepository.count();
    }

    private DocumentMetadataResponse toResponse(DocumentMetadataNativeResponse doc) {
        String fileType = getExtension(doc.getFileName());
        String previewUrl = buildPreviewUrl(fileType, doc.getId());
        return new DocumentMetadataResponse(
                doc.getId(),
                doc.getFileName(),
                doc.getPublicId(),
                doc.getUrl(),
                doc.getFileSize(),
                doc.getUploadedBy(),
                doc.getUploadedAt(),
                doc.getCommitMessage(),
                doc.getVersion(),
                fileType,
                previewUrl
        );
    }

    private String buildPreviewUrl(String fileType, Long id) {
        String streamUrl = "http://localhost:8080/files/" + id + "/stream";
        if ("pdf".equals(fileType)) {
            return streamUrl;
        }
        if (OFFICE_TYPES.contains(fileType)) {
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
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
