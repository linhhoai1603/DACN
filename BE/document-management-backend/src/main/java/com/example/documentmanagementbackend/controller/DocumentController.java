package com.example.documentmanagementbackend.controller;

import com.example.documentmanagementbackend.dto.ApiResponse;
import com.example.documentmanagementbackend.dto.request.CreateDocumentRequest;
import com.example.documentmanagementbackend.dto.response.DocumentSummaryResponse;
import com.example.documentmanagementbackend.dto.response.DocumentVersionResponse;
import com.example.documentmanagementbackend.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<DocumentSummaryResponse>>> getMyDocuments(Authentication authentication) {
        String username = authentication.getName();
        List<DocumentSummaryResponse> response = documentService.getMyDocuments(username);
        return ResponseEntity.ok(ApiResponse.success("My documents fetched successfully", response));
    }

    @GetMapping("/{documentId}/versions")
    public ResponseEntity<ApiResponse<List<DocumentVersionResponse>>> getDocumentVersions(
            @PathVariable UUID documentId,
            Authentication authentication
    ) {
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        List<DocumentVersionResponse> response = documentService.getDocumentVersions(documentId, username, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Document versions fetched successfully", response));
    }

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
