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

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "commitMessage", required = false) String commitMessage,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String uploadedBy = (userDetails != null) ? userDetails.getUsername() : "anonymous";
        FileUploadResponse response = fileUploadService.upload(file, uploadedBy, commitMessage);
        return ResponseEntity.ok(response);
    }
}
