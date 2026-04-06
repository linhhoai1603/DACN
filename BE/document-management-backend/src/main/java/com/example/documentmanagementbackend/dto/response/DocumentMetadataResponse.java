package com.example.documentmanagementbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DocumentMetadataResponse {
    private Long id;
    private String fileName;
    private String publicId;
    private String url;
    private Long fileSize;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
    private String commitMessage;
    private String version;
    private String fileType; // pdf, docx, xlsx, doc, xls
    private String previewUrl; // URL để preview (Google Docs Viewer cho office files)
}
