package com.example.documentmanagementbackend.dto.response;

import com.example.documentmanagementbackend.entity.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSummaryResponse {
    private UUID documentId;
    private String title;
    private String description;
    private DocumentStatus status;
    private Integer latestVersionNumber;
    private Instant createdAt;
    private String createdBy;
}
