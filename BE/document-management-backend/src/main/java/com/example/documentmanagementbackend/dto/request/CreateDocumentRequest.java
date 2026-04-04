package com.example.documentmanagementbackend.dto.request;

import com.example.documentmanagementbackend.entity.DocumentStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDocumentRequest {

    @NotBlank(message = "title is required")
    private String title;

    private String description;

    private String commitMessage;

    private DocumentStatus status = DocumentStatus.DRAFT;
}
