package com.example.documentmanagementbackend.service.impl;

import com.example.documentmanagementbackend.dto.request.CreateDocumentRequest;
import com.example.documentmanagementbackend.dto.response.DocumentSummaryResponse;
import com.example.documentmanagementbackend.dto.response.DocumentVersionResponse;
import com.example.documentmanagementbackend.dto.response.ImageKitUploadResponse;
import com.example.documentmanagementbackend.entity.Document;
import com.example.documentmanagementbackend.entity.DocumentVersion;
import com.example.documentmanagementbackend.exception.BadRequestException;
import com.example.documentmanagementbackend.exception.ResourceNotFoundException;
import com.example.documentmanagementbackend.repository.DocumentRepository;
import com.example.documentmanagementbackend.repository.DocumentVersionRepository;
import com.example.documentmanagementbackend.service.DocumentService;
import com.example.documentmanagementbackend.service.ImageKitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final ImageKitService imageKitService;

    @Override
    @Transactional
    public DocumentVersionResponse createDocumentWithInitialVersion(CreateDocumentRequest request, MultipartFile file, String username) {
        validateFile(file);

        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setDescription(request.getDescription());
        document.setStatus(request.getStatus());
        document.setIsDeleted(false);
        document = documentRepository.save(document);

        String commitMessage = (request.getCommitMessage() == null || request.getCommitMessage().isBlank())
                ? "Initial version"
                : request.getCommitMessage();

        return persistVersion(document, 1, commitMessage, file, username);
    }

    @Override
    @Transactional
    public DocumentVersionResponse uploadNewVersion(UUID documentId, String commitMessage, MultipartFile file, String username) {
        validateFile(file);
        if (commitMessage == null || commitMessage.isBlank()) {
            throw new BadRequestException("commitMessage is required");
        }

        // Lock document row to avoid two parallel uploads creating the same version number.
        Document document = documentRepository.findActiveByIdForUpdate(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

        int nextVersion = documentVersionRepository.findTopByDocumentOrderByVersionNumberDesc(document)
                .map(v -> v.getVersionNumber() + 1)
                .orElse(1);

        return persistVersion(document, nextVersion, commitMessage, file, username);
    }

        @Override
        @Transactional(readOnly = true)
        public List<DocumentSummaryResponse> getMyDocuments(String username) {
        return documentRepository.findByCreatedByAndIsDeletedFalseOrderByCreatedAtDesc(username)
            .stream()
            .map(document -> {
                Integer latestVersion = documentVersionRepository.findTopByDocumentOrderByVersionNumberDesc(document)
                    .map(DocumentVersion::getVersionNumber)
                    .orElse(0);
                return new DocumentSummaryResponse(
                    document.getId(),
                    document.getTitle(),
                    document.getDescription(),
                    document.getStatus(),
                    latestVersion,
                    document.getCreatedAt(),
                    document.getCreatedBy()
                );
            })
            .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public List<DocumentVersionResponse> getDocumentVersions(UUID documentId, String username, boolean isAdmin) {
        Document document;
        if (isAdmin) {
            document = documentRepository.findByIdAndIsDeletedFalse(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));
        } else {
            document = documentRepository.findByIdAndCreatedByAndIsDeletedFalse(documentId, username)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));
        }

        return documentVersionRepository.findByDocumentIdOrderByVersionNumberDesc(document.getId())
            .stream()
            .map(version -> new DocumentVersionResponse(
                version.getId(),
                document.getId(),
                version.getVersionNumber(),
                version.getFileUrl(),
                version.getFileId(),
                version.getFileName(),
                version.getFileSize(),
                version.getFileHash(),
                version.getCommitMessage(),
                version.getCreatedBy(),
                version.getCreatedAt()
            ))
            .toList();
        }

    private DocumentVersionResponse persistVersion(Document document, int version, String commitMessage, MultipartFile file, String username) {
        ImageKitUploadResponse uploaded = imageKitService.uploadFile(file, "/documents/" + document.getId());

        DocumentVersion entity = new DocumentVersion();
        entity.setDocument(document);
        entity.setVersionNumber(version);
        entity.setFileUrl(uploaded.getFileUrl());
        entity.setFileId(uploaded.getFileId());
        entity.setFileName(uploaded.getFileName());
        entity.setFileSize(uploaded.getFileSize());
        entity.setFileHash(sha256(file));
        entity.setCommitMessage(commitMessage);

        DocumentVersion saved = documentVersionRepository.save(entity);

        return new DocumentVersionResponse(
                saved.getId(),
                document.getId(),
                saved.getVersionNumber(),
                saved.getFileUrl(),
                saved.getFileId(),
                saved.getFileName(),
                saved.getFileSize(),
                saved.getFileHash(),
                saved.getCommitMessage(),
                username,
                saved.getCreatedAt()
        );
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
    }

    private String sha256(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(file.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new BadRequestException("Cannot compute file hash");
        }
    }
}
