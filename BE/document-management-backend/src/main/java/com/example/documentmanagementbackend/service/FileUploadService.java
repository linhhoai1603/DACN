package com.example.documentmanagementbackend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.documentmanagementbackend.dto.response.FileUploadResponse;
import com.example.documentmanagementbackend.model.DocumentMetadata;
import com.example.documentmanagementbackend.repository.DocumentMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class FileUploadService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx", "xls", "xlsx");

    private final Cloudinary cloudinary;
    private final DocumentMetadataRepository metadataRepository;

    public FileUploadService(Cloudinary cloudinary, DocumentMetadataRepository metadataRepository) {
        this.cloudinary = cloudinary;
        this.metadataRepository = metadataRepository;
    }

    public FileUploadResponse upload(MultipartFile file, String uploadedBy, String commitMessage) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = getExtension(originalFilename);

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new RuntimeException("Unsupported file type: " + ext + ". Allowed: pdf, doc, docx, xls, xlsx");
        }

        // Tính version: đếm số lần upload cùng tên file + 1
        List<DocumentMetadata> existing = metadataRepository.findByFileNameOrderByUploadedAtDesc(originalFilename);
        int versionNumber = existing.size() + 1;
        String version = "v" + versionNumber;

        // public_id trên Cloudinary: tên file + version để tránh ghi đè
        String publicId = "documents/" + stripExtension(originalFilename) + "_" + version;

        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "raw",
                            "public_id", publicId,
                            "use_filename", false,
                            "unique_filename", false
                    )
            );

            String url = uploadResult.get("secure_url").toString();
            String returnedPublicId = uploadResult.get("public_id").toString();
            LocalDateTime now = LocalDateTime.now();
            String commit = (commitMessage == null || commitMessage.isBlank()) ? "init file" : commitMessage;
            String uploader = (uploadedBy == null || uploadedBy.isBlank()) ? "anonymous" : uploadedBy;

            // Lưu metadata
            DocumentMetadata metadata = DocumentMetadata.builder()
                    .fileName(originalFilename)
                    .publicId(returnedPublicId)
                    .url(url)
                    .fileSize(file.getSize())
                    .uploadedBy(uploader)
                    .uploadedAt(now)
                    .commitMessage(commit)
                    .version(version)
                    .build();

            metadataRepository.save(metadata);

            return new FileUploadResponse(url, returnedPublicId, originalFilename, file.getSize(), version, uploader, now, commit);

        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage());
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private String stripExtension(String filename) {
        if (filename == null || !filename.contains(".")) return filename;
        return filename.substring(0, filename.lastIndexOf('.'));
    }
}
