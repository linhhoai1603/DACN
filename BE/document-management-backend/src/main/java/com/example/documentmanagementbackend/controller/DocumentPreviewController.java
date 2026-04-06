package com.example.documentmanagementbackend.controller;

import com.example.documentmanagementbackend.dto.response.DocumentMetadataResponse;
import com.example.documentmanagementbackend.service.DocumentPreviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/files")
public class DocumentPreviewController {

    private final DocumentPreviewService previewService;

    public DocumentPreviewController(DocumentPreviewService previewService) {
        this.previewService = previewService;
    }

    // GET /files — lấy toàn bộ danh sách documents
    @GetMapping
    public ResponseEntity<List<DocumentMetadataResponse>> getAllDocuments() {
        return ResponseEntity.ok(previewService.getAll());
    }

    // GET /files/{id} — lấy metadata + previewUrl của một document
    @GetMapping("/{id}")
    public ResponseEntity<DocumentMetadataResponse> getDocumentById(@PathVariable Long id) {
        return ResponseEntity.ok(previewService.getById(id));
    }
}
