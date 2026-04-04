package com.example.documentmanagementbackend.service;

import com.example.documentmanagementbackend.dto.request.CreateDocumentRequest;
import com.example.documentmanagementbackend.dto.response.DocumentVersionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface DocumentService {
    DocumentVersionResponse createDocumentWithInitialVersion(CreateDocumentRequest request, MultipartFile file, String username);

    DocumentVersionResponse uploadNewVersion(UUID documentId, String commitMessage, MultipartFile file, String username);
}
