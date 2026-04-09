package com.example.documentmanagementbackend.controller;

import com.example.documentmanagementbackend.dto.response.DocumentMetadataResponse;
import com.example.documentmanagementbackend.service.DocumentService;
import com.example.documentmanagementbackend.service.DocumentPreviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/files")
public class DocumentPreviewController {

    private final DocumentPreviewService previewService;
    private final DocumentService documentService;

    public DocumentPreviewController(DocumentPreviewService previewService, DocumentService documentService) {
        this.previewService = previewService;
        this.documentService = documentService;
    }

    // GET /files — lấy toàn bộ danh sách documents
    @GetMapping
    public ResponseEntity<List<DocumentMetadataResponse>> getAllDocuments() {
        return ResponseEntity.ok(previewService.getAll());
    }

    // GET /files?page=0&index=10 — trả danh sách DocumentMetadataResponse theo phân trang
    @GetMapping(params = {"index","page"})
    public ResponseEntity<List<DocumentMetadataResponse>> getDocumentViews(
            @RequestParam int index,
            @RequestParam int page
    ) {
        List<DocumentMetadataResponse> documents = documentService.getDocuments(page, index);
        if (documents.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countDocuments() {
        return ResponseEntity.ok(documentService.countDocuments());
    }

    // GET /files/{id} — lấy metadata + previewUrl của một document
    @GetMapping("/{id}")
    public ResponseEntity<DocumentMetadataResponse> getDocumentById(@PathVariable Long id) {
        return ResponseEntity.ok(previewService.getById(id));
    }
}
