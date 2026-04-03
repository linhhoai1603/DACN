package com.example.documentmanagementbackend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.documentmanagementbackend.dto.response.FileUploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Service
public class FileUploadService {

    private final Cloudinary cloudinary;

    public FileUploadService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public FileUploadResponse upload(MultipartFile file, Long userId, String email, String fullName, String role) {

        System.out.println("userId: " + userId);
        System.out.println("email: " + email);
        System.out.println("fullName: " + fullName);
        System.out.println("role: " + role);

        try {
            if (file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }

            // Lấy tên file gốc (giữ extension)
            String originalFilename = file.getOriginalFilename();

            log.info("Upload initiated by userId={}, email={}", userId, email);

            // Upload file
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "raw",
                            "folder", "documents",
                            "public_id", file.getOriginalFilename()
                    )
            );

            String publicId = uploadResult.get("public_id").toString();

            log.info("Upload completed: publicId={}, userId={}, email={}", publicId, userId, email);

            return new FileUploadResponse(
                    uploadResult.get("secure_url").toString(),
                    publicId,
                    originalFilename,
                    file.getSize()
            );

        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage());
        }
    }
}