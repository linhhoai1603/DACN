package com.example.documentmanagementbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageKitUploadResponse {
    private String fileId;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
}
