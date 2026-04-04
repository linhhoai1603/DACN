package com.example.documentmanagementbackend.repository;

import com.example.documentmanagementbackend.entity.Document;
import com.example.documentmanagementbackend.entity.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

    Optional<DocumentVersion> findTopByDocumentOrderByVersionNumberDesc(Document document);

    List<DocumentVersion> findByDocumentIdOrderByVersionNumberDesc(UUID documentId);
}
