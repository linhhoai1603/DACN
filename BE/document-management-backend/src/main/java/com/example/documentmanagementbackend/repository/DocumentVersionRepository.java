package com.example.documentmanagementbackend.repository;

import com.example.documentmanagementbackend.model.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

    List<DocumentVersion> findByDocumentIdOrderByVersionNumberDesc(Long documentId);

    @Query("SELECT MAX(v.versionNumber) FROM DocumentVersion v WHERE v.documentId = :docId")
    Integer findMaxVersionNumber(@Param("docId") Long docId);

    @Modifying
    @Query("UPDATE DocumentVersion v SET v.isLatest = false WHERE v.documentId = :docId")
    void clearLatestByDocumentId(@Param("docId") Long docId);
}
