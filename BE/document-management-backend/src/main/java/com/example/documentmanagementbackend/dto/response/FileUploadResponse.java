package com.example.documentmanagementbackend.dto.response;

import lombok.Getter;

@Getter
public class FileUploadResponse {
    private String url;
    private String publicId;
    private String fileName;
    private long size;

    public FileUploadResponse(String url, String publicId, String fileName, long size) {
        this.url = url;
        this.publicId = publicId;
        this.fileName = fileName;
        this.size = size;
    }

}
