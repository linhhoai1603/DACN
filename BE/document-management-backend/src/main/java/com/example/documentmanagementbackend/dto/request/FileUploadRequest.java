package com.example.documentmanagementbackend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileUploadRequest {
    private String commitMessage;
    private String uploadedBy;
}
