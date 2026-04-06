package com.example.documentmanagementbackend.controller;

import com.example.documentmanagementbackend.dto.response.FileUploadResponse;
import com.example.documentmanagementbackend.service.FileUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    /**
     * Upload file mới lần đầu.
     * POST /files/upload
     * Dùng bởi: UploadDashboard
     */
    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "commitMessage", required = false) String commitMessage,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String uploadedBy = userDetails != null ? userDetails.getUsername() : "anonymous";
        return ResponseEntity.ok(fileUploadService.upload(file, uploadedBy, commitMessage));
    }

    /**
     * Update version mới cho document đã tồn tại.
     * PUT /files/{id}/update
     * Dùng bởi: UpdateDocument
     */
    @PutMapping("/{id}/update")
    public ResponseEntity<FileUploadResponse> updateVersion(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "commitMessage", required = false) String commitMessage,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String uploadedBy = userDetails != null ? userDetails.getUsername() : "anonymous";
        return ResponseEntity.ok(fileUploadService.updateVersion(id, file, uploadedBy, commitMessage));
    }
}
