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
        byte[] bytes = fetchBytes(doc, ext);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mimeType));
        headers.setContentLength(bytes.length);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"");
        headers.set("Content-Security-Policy", "frame-ancestors *");
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        DocumentMetadata doc = metadataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));

        String ext = getExtension(doc.getFileName());
        String mimeType = MIME_TYPES.getOrDefault(ext, "application/octet-stream");
        byte[] bytes = fetchBytes(doc, ext);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mimeType));
        headers.setContentLength(bytes.length);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"");
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    /**
     * Fetch file bytes.
     * Thử 3 cách theo thứ tự:
     * 1. URL gốc (public)
     * 2. Signed delivery URL (res.cloudinary.com với chữ ký)
     * 3. Authenticated fetch với Basic Auth (api_key:api_secret)
     */
    private byte[] fetchBytes(DocumentMetadata doc, String ext) {
        // Cách 1: URL gốc
        byte[] result = tryFetch(doc.getUrl());
        if (result != null) return result;

        // Cách 2: Signed delivery URL
        try {
            String signedDeliveryUrl = cloudinary.url()
                    .resourceType("raw")
                    .type("upload")
                    .signed(true)
                    .generate(doc.getPublicId());
            System.out.println("[DEBUG] signed delivery URL: " + signedDeliveryUrl);
            result = tryFetch(signedDeliveryUrl);
            if (result != null) return result;
        } catch (Exception e) {
            System.out.println("[DEBUG] signed URL error: " + e.getMessage());
        }

        // Cách 3: fetch URL gốc với Basic Auth (api_key:api_secret)
        try {
            String apiKey = (String) cloudinary.config.apiKey;
            String apiSecret = (String) cloudinary.config.apiSecret;
            result = tryFetchWithAuth(doc.getUrl(), apiKey, apiSecret);
            if (result != null) return result;
        } catch (Exception e) {
            System.out.println("[DEBUG] auth fetch error: " + e.getMessage());
        }

        throw new RuntimeException("Cannot access file from storage: " + doc.getFileName());
    }

    private byte[] tryFetchWithAuth(String urlStr, String apiKey, String apiSecret) {
        try {
            String credentials = apiKey + ":" + apiSecret;
            String encoded = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(60000);
            conn.setRequestProperty("Authorization", "Basic " + encoded);
            conn.setInstanceFollowRedirects(true);
            int status = conn.getResponseCode();
            System.out.println("[DEBUG] tryFetchWithAuth " + urlStr + " → HTTP " + status);
            if (status == 200) {
                try (InputStream is = conn.getInputStream()) {
                    return is.readAllBytes();
                }
            }
        } catch (Exception e) {
            System.out.println("[DEBUG] tryFetchWithAuth error: " + e.getMessage());
        }
        return null;
    }

    private byte[] tryFetch(String urlStr) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(60000);
            conn.setRequestProperty("User-Agent", "DocuManage/1.0");
            conn.setInstanceFollowRedirects(true);
            int status = conn.getResponseCode();
            System.out.println("[DEBUG] tryFetch " + urlStr + " → HTTP " + status);
            if (status == 200) {
                try (InputStream is = conn.getInputStream()) {
                    return is.readAllBytes();
                }
            }
        } catch (Exception e) {
            System.out.println("[DEBUG] tryFetch error: " + e.getMessage());
        }
        return null;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
