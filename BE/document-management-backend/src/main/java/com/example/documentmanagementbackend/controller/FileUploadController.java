package com.example.documentmanagementbackend.controller;

import com.example.documentmanagementbackend.dto.response.FileUploadResponse;
import com.example.documentmanagementbackend.model.User;
import com.example.documentmanagementbackend.service.FileUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
            @RequestParam("file") MultipartFile file
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        if (!(authentication.getPrincipal() instanceof User currentUser)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        FileUploadResponse response = fileUploadService.upload(
                file,
                currentUser.getId(),
                currentUser.getEmail(),
                currentUser.getFullName(),
                currentUser.getRole().name()
        );
        return ResponseEntity.ok(response);
    }
}
