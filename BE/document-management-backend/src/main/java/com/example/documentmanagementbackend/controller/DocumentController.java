package com.example.documentmanagementbackend.controller;

import com.example.documentmanagementbackend.dto.ApiResponse;
import com.example.documentmanagementbackend.dto.request.CreateDocumentRequest;
import com.example.documentmanagementbackend.dto.response.DocumentVersionResponse;
import com.example.documentmanagementbackend.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentVersionResponse>> createDocument(
            @Valid @RequestPart("payload") CreateDocumentRequest request,
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) {
        String username = authentication.getName();
        DocumentVersionResponse response = documentService.createDocumentWithInitialVersion(request, file, username);
        return ResponseEntity.ok(ApiResponse.success("Document created with initial version", response));
    }

    @PostMapping(value = "/{documentId}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentVersionResponse>> uploadVersion(
            @PathVariable UUID documentId,
            @RequestPart("commitMessage") String commitMessage,
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) {
        String username = authentication.getName();
        DocumentVersionResponse response = documentService.uploadNewVersion(documentId, commitMessage, file, username);
        return ResponseEntity.ok(ApiResponse.success("New document version uploaded", response));
    }
}
