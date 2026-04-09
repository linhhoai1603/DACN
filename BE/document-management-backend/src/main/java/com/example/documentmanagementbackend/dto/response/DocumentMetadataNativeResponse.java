package com.example.documentmanagementbackend.dto.response;

import java.time.LocalDateTime;

public interface DocumentMetadataNativeResponse {
    Long getId();
    String getFileName();
    String getPublicId();
    String getUrl();
    Long getFileSize();
    String getUploadedBy();
    LocalDateTime getUploadedAt();
    String getCommitMessage();
    String getVersion();
}
