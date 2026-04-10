package com.example.documentmanagementbackend.controller;

import com.example.documentmanagementbackend.dto.response.DocumentVersionResponse;
import com.example.documentmanagementbackend.service.DocumentVersionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/files")
public class DocumentVersionController {

    private final DocumentVersionService documentVersionService;

    public DocumentVersionController(DocumentVersionService documentVersionService) {
        this.documentVersionService = documentVersionService;
    }

    /**
     * GET /files/{id}/versions
     * Trả về toàn bộ version history của document theo id.
     */
    @GetMapping("/{id}/versions")
    public ResponseEntity<List<DocumentVersionResponse>> getVersions(@PathVariable Long id) {
        return ResponseEntity.ok(documentVersionService.getVersionsByDocumentId(id));
    }
}
