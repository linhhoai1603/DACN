package com.example.documentmanagementbackend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.documentmanagementbackend.dto.response.FileUploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class FileUploadService {

    private final Cloudinary cloudinary;

    public FileUploadService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public FileUploadResponse upload(MultipartFile file) {

        try {
            if (file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }

            // Lấy tên file gốc (giữ extension)
            String originalFilename = file.getOriginalFilename();

            // Upload file
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "raw",
                            "folder", "documents",
                            "public_id", file.getOriginalFilename()
                    )
            );

            return new FileUploadResponse(
                    uploadResult.get("secure_url").toString(),
                    uploadResult.get("public_id").toString(),
                    originalFilename,
                    file.getSize()
            );

        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage());
        }
    }
}