package com.example.documentmanagementbackend.repository;

import com.example.documentmanagementbackend.model.DocumentMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentMetadataRepository extends JpaRepository<DocumentMetadata, Long> {
    List<DocumentMetadata> findByFileNameOrderByUploadedAtDesc(String fileName);
}
