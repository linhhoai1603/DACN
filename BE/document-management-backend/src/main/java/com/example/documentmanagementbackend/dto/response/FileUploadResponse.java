package com.example.documentmanagementbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class FileUploadResponse {
    private String url;
    private String publicId;
    private String fileName;
    private long size;
    private String version;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
    private String commitMessage;
}
