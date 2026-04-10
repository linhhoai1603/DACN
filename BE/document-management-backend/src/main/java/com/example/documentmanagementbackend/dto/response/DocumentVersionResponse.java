package com.example.documentmanagementbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DocumentVersionResponse {
    private Long id;
    private Integer versionNumber;
    private String version;       // "v1", "v2", ...
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
    private String commitMessage;
    private Boolean isLatest;
}
