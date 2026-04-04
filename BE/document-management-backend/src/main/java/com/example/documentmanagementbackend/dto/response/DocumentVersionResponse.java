package com.example.documentmanagementbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVersionResponse {
    private Long versionId;
    private UUID documentId;
    private Integer versionNumber;
    private String fileUrl;
    private String fileId;
    private String fileName;
    private Long fileSize;
    private String fileHash;
    private String commitMessage;
    private String createdBy;
    private Instant createdAt;
}
