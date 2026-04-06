package com.example.documentmanagementbackend.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.documentmanagementbackend.model.DocumentMetadata;
import com.example.documentmanagementbackend.repository.DocumentMetadataRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;

@RestController
@RequestMapping("/files")
public class DocumentProxyController {

    private static final Map<String, String> MIME_TYPES = Map.of(
            "pdf",  "application/pdf",
            "doc",  "application/msword",
            "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "xls",  "application/vnd.ms-excel",
            "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private final DocumentMetadataRepository metadataRepository;
    private final Cloudinary cloudinary;

    public DocumentProxyController(DocumentMetadataRepository metadataRepository, Cloudinary cloudinary) {
        this.metadataRepository = metadataRepository;
        this.cloudinary = cloudinary;
    }

    @GetMapping("/{id}/stream")
    public ResponseEntity<byte[]> streamFile(@PathVariable Long id) {
        DocumentMetadata doc = metadataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));

        String ext = getExtension(doc.getFileName());
        String mimeType = MIME_TYPES.getOrDefault(ext, "application/octet-stream");

        // Thử fetch trực tiếp trước
        byte[] bytes = tryFetchBytes(doc.getUrl());

        if (bytes == null) {
            // File bị authenticated — dùng privateDownload URL (signed, có thời hạn)
            try {
                String publicIdWithExt = buildPublicIdWithExt(doc);
                String privateUrl = cloudinary.privateDownload(publicIdWithExt, ext,
                        ObjectUtils.asMap("resource_type", "raw"));
                bytes = tryFetchBytes(privateUrl);
            } catch (Exception ignored) {}
        }

        if (bytes == null) {
            // Redirect về privateDownload URL — browser tự fetch
            try {
                String publicIdWithExt = buildPublicIdWithExt(doc);
                String privateUrl = cloudinary.privateDownload(publicIdWithExt, ext,
                        ObjectUtils.asMap("resource_type", "raw"));
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(privateUrl))
                        .build();
            } catch (Exception e) {
                throw new RuntimeException("Cannot access file. Please check Cloudinary settings: " + doc.getFileName());
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mimeType));
        headers.setContentLength(bytes.length);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"");
        headers.set("X-Frame-Options", "ALLOWALL");
        headers.set("Content-Security-Policy", "frame-ancestors *");
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadFile(@PathVariable Long id) {
        DocumentMetadata doc = metadataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));

        String ext = getExtension(doc.getFileName());
        String mimeType = MIME_TYPES.getOrDefault(ext, "application/octet-stream");

        byte[] bytes = tryFetchBytes(doc.getUrl());

        if (bytes != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(mimeType));
            headers.setContentLength(bytes.length);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"");
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        }

        // Fallback: redirect về privateDownload
        try {
            String publicIdWithExt = buildPublicIdWithExt(doc);
            String privateUrl = cloudinary.privateDownload(publicIdWithExt, ext,
                    ObjectUtils.asMap("resource_type", "raw"));
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(privateUrl))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Cannot download file: " + doc.getFileName());
        }
    }

    private String buildPublicIdWithExt(DocumentMetadata doc) {
        String ext = getExtension(doc.getFileName());
        String publicId = doc.getPublicId();
        if (!ext.isEmpty() && publicId.endsWith("." + ext)) {
            return publicId; // đã có extension
        }
        return publicId + "." + ext;
    }

    private byte[] tryFetchBytes(String urlStr) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(60000);
            conn.setRequestProperty("User-Agent", "DocuManage/1.0");
            conn.setInstanceFollowRedirects(true);
            if (conn.getResponseCode() == 200) {
                try (InputStream is = conn.getInputStream()) {
                    return is.readAllBytes();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
