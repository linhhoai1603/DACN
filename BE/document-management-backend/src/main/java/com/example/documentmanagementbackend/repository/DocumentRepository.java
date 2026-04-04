package com.example.documentmanagementbackend.repository;

import com.example.documentmanagementbackend.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    Optional<Document> findByIdAndIsDeletedFalse(UUID id);

    List<Document> findByCreatedByAndIsDeletedFalseOrderByCreatedAtDesc(String createdBy);

    Optional<Document> findByIdAndCreatedByAndIsDeletedFalse(UUID id, String createdBy);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Document d where d.id = :id and d.isDeleted = false")
    Optional<Document> findActiveByIdForUpdate(@Param("id") UUID id);
}
