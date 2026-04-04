package com.example.documentmanagementbackend.service;

import com.example.documentmanagementbackend.dto.response.ImageKitUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ImageKitService {
    ImageKitUploadResponse uploadFile(MultipartFile file, String folder);
}
