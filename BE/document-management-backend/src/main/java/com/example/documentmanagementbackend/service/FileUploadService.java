package com.example.documentmanagementbackend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.documentmanagementbackend.dto.response.FileUploadResponse;
import com.example.documentmanagementbackend.model.DocumentMetadata;
import com.example.documentmanagementbackend.model.DocumentVersion;
import com.example.documentmanagementbackend.repository.DocumentMetadataRepository;
import com.example.documentmanagementbackend.repository.DocumentVersionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Service
public class FileUploadService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx", "xls", "xlsx");

    private final Cloudinary cloudinary;
    private final DocumentMetadataRepository metadataRepository;
    private final DocumentVersionRepository versionRepository;

    public FileUploadService(Cloudinary cloudinary,
                             DocumentMetadataRepository metadataRepository,
                             DocumentVersionRepository versionRepository) {
        this.cloudinary = cloudinary;
        this.metadataRepository = metadataRepository;
        this.versionRepository = versionRepository;
    }

    // ── POST /files/upload — Upload file mới ────────────────────────────────
    @Transactional
    public FileUploadResponse upload(MultipartFile file, String uploadedBy, String commitMessage) {
        validateFile(file);

        String filename = file.getOriginalFilename();
        String ext = getExtension(filename);
        LocalDateTime now = LocalDateTime.now();
        String commit = blankDefault(commitMessage, "init file");
        String uploader = blankDefault(uploadedBy, "anonymous");

        // Upload lên Cloudinary với version = 1
        String publicId = buildPublicId(filename, ext, 1);
        Map uploadResult = uploadToCloudinary(file, publicId);
        String url = uploadResult.get("secure_url").toString();
        String returnedPublicId = uploadResult.get("public_id").toString();

        // Tạo record trong document_metadata (bảng chính)
        DocumentMetadata metadata = DocumentMetadata.builder()
                .fileName(filename)
                .publicId(returnedPublicId)
                .url(url)
                .fileSize(file.getSize())
                .uploadedBy(uploader)
                .uploadedAt(now)
                .commitMessage(commit)
                .version("v1")
                .createdAt(now)
                .build();
        metadata = metadataRepository.save(metadata);

        // Tạo record đầu tiên trong document_versions
        DocumentVersion v1 = DocumentVersion.builder()
                .documentId(metadata.getId())
                .versionNumber(1)
                .fileName(filename)
                .fileUrl(url)
                .publicId(returnedPublicId)
                .fileSize(file.getSize())
                .uploadedBy(uploader)
                .uploadedAt(now)
                .commitMessage(commit)
                .isLatest(true)
                .build();
        v1 = versionRepository.save(v1);

        // Cập nhật FK current_version_id
        metadata.setCurrentVersionId(v1.getId());
        metadataRepository.save(metadata);

        return toResponse(url, returnedPublicId, filename, file.getSize(), 1, uploader, now, commit);
    }

    // ── PUT /files/{id}/update — Update version mới ─────────────────────────
    @Transactional
    public FileUploadResponse updateVersion(Long documentId, MultipartFile file,
                                            String uploadedBy, String commitMessage) {
        DocumentMetadata metadata = metadataRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));

        validateFile(file);

        String filename = file.getOriginalFilename();
        String ext = getExtension(filename);
        LocalDateTime now = LocalDateTime.now();
        String commit = blankDefault(commitMessage, "update");
        String uploader = blankDefault(uploadedBy, "anonymous");

        // Tính version tiếp theo — ưu tiên lấy từ document_versions,
        // fallback về parse version string trong document_metadata (xử lý cả "v1", "vv1", "1")
        Integer maxVer = versionRepository.findMaxVersionNumber(metadata.getId());
        int nextVersion;
        if (maxVer != null) {
            nextVersion = maxVer + 1;
        } else {
            nextVersion = parseVersionNumber(metadata.getVersion()) + 1;
        }

        // Upload file mới lên Cloudinary
        String publicId = buildPublicId(metadata.getFileName(), ext, nextVersion);
        Map uploadResult = uploadToCloudinary(file, publicId);
        String url = uploadResult.get("secure_url").toString();
        String returnedPublicId = uploadResult.get("public_id").toString();

        // Đánh dấu tất cả version cũ là không phải latest
        versionRepository.clearLatestByDocumentId(metadata.getId());

        // Tạo version mới trong document_versions
        DocumentVersion newVer = DocumentVersion.builder()
                .documentId(metadata.getId())
                .versionNumber(nextVersion)
                .fileName(filename)
                .fileUrl(url)
                .publicId(returnedPublicId)
                .fileSize(file.getSize())
                .uploadedBy(uploader)
                .uploadedAt(now)
                .commitMessage(commit)
                .isLatest(true)
                .build();
        newVer = versionRepository.save(newVer);

        // Cập nhật document_metadata với thông tin version mới
        metadata.setFileName(filename);
        metadata.setPublicId(returnedPublicId);
        metadata.setUrl(url);
        metadata.setFileSize(file.getSize());
        metadata.setUploadedBy(uploader);
        metadata.setUploadedAt(now);
        metadata.setCommitMessage(commit);
        metadata.setVersion("v" + nextVersion);
        metadata.setCurrentVersionId(newVer.getId());        metadataRepository.save(metadata);

        return toResponse(url, returnedPublicId, filename, file.getSize(), nextVersion, uploader, now, commit);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) throw new RuntimeException("File is empty");
        String ext = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext))
            throw new RuntimeException("Unsupported file type: " + ext + ". Allowed: pdf, doc, docx, xls, xlsx");
    }

    private Map uploadToCloudinary(MultipartFile file, String publicId) {
        try {
            return cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "raw",
                            "type", "upload",
                            "public_id", publicId,
                            "use_filename", false,
                            "unique_filename", false
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException("Cloudinary upload failed: " + e.getMessage());
        }
    }

    private String buildPublicId(String filename, String ext, int version) {
        return "documents/" + stripExtension(filename) + "_v" + version + "." + ext;
    }

    private FileUploadResponse toResponse(String url, String publicId, String fileName,
                                           long size, int version, String uploadedBy,
                                           LocalDateTime uploadedAt, String commitMessage) {
        return new FileUploadResponse(url, publicId, fileName, size,
                "v" + version, uploadedBy, uploadedAt, commitMessage);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private String stripExtension(String filename) {
        if (filename == null || !filename.contains(".")) return filename;
        return filename.substring(0, filename.lastIndexOf('.'));
    }

    private String blankDefault(String value, String defaultVal) {
        return (value == null || value.isBlank()) ? defaultVal : value;
    }

    /**
     * Parse version number từ string bất kỳ: "v1", "vv1", "V2", "3" → trả về số nguyên.
     * Nếu không parse được thì trả về 1.
     */
    private int parseVersionNumber(String version) {
        if (version == null || version.isBlank()) return 1;
        // Strip tất cả ký tự không phải số ở đầu (v, V, vv, ...)
        String digits = version.replaceAll("^[^0-9]+", "");
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
