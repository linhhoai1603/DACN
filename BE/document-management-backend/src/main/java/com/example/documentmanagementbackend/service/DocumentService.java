package com.example.documentmanagementbackend.service;

import com.example.documentmanagementbackend.dto.response.DocumentMetadataResponse;

import java.util.List;

public interface DocumentService {
    List<DocumentMetadataResponse> getDocuments(int index, int page);
    long countDocuments();
    List<DocumentMetadataResponse> searchDocuments(String keyword);
}
