package com.example.documentmanagementbackend.versionControlListTest;

import com.example.documentmanagementbackend.controller.DocumentPreviewController;
import com.example.documentmanagementbackend.dto.response.DocumentMetadataResponse;
import com.example.documentmanagementbackend.service.DocumentPreviewService;
import com.example.documentmanagementbackend.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentPreviewControllerTest {

    @Mock
    private DocumentPreviewService previewService;

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private DocumentPreviewController controller;

    private DocumentMetadataResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = new DocumentMetadataResponse(
                1L, "report.pdf", "pub1",
                "http://cdn.example.com/report.pdf",
                1024L, "alice",
                LocalDateTime.of(2024, 1, 1, 10, 0),
                "initial", "v1", "pdf",
                "http://localhost:8080/files/1/stream"
        );
    }

    // ── GET /files ────────────────────────────────────────────────────────────

    @Test
    void getAllDocuments_returnsOkWithList() {
        when(previewService.getAll()).thenReturn(List.of(sampleResponse));

        ResponseEntity<List<DocumentMetadataResponse>> response = controller.getAllDocuments();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getAllDocuments_emptyList_returnsOkWithEmptyBody() {
        when(previewService.getAll()).thenReturn(List.of());

        ResponseEntity<List<DocumentMetadataResponse>> response = controller.getAllDocuments();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    // ── GET /files?page=&index= ───────────────────────────────────────────────

    @Test
    void getDocumentViews_withResults_returnsOk() {
        when(documentService.getDocuments(0, 10)).thenReturn(List.of(sampleResponse));

        ResponseEntity<List<DocumentMetadataResponse>> response = controller.getDocumentViews(10, 0);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getDocumentViews_emptyResult_returnsNotFound() {
        when(documentService.getDocuments(0, 10)).thenReturn(List.of());

        ResponseEntity<List<DocumentMetadataResponse>> response = controller.getDocumentViews(10, 0);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getDocumentViews_multipleDocuments_returnsAll() {
        DocumentMetadataResponse second = new DocumentMetadataResponse(
                2L, "letter.docx", "pub2",
                "http://cdn.example.com/letter.docx",
                2048L, "bob",
                LocalDateTime.of(2024, 2, 1, 9, 0),
                "draft", "v1", "docx",
                "https://docs.google.com/viewer?embedded=true&url=..."
        );
        when(documentService.getDocuments(1, 5)).thenReturn(List.of(sampleResponse, second));

        ResponseEntity<List<DocumentMetadataResponse>> response = controller.getDocumentViews(5, 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    // ── GET /files/count ──────────────────────────────────────────────────────

    @Test
    void countDocuments_returnsOkWithCount() {
        when(documentService.countDocuments()).thenReturn(42L);

        ResponseEntity<Long> response = controller.countDocuments();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(42L);
    }

    @Test
    void countDocuments_zeroDocuments_returnsZero() {
        when(documentService.countDocuments()).thenReturn(0L);

        ResponseEntity<Long> response = controller.countDocuments();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(0L);
    }

    // ── GET /files/{id} ───────────────────────────────────────────────────────

    @Test
    void getDocumentById_existingId_returnsOk() {
        when(previewService.getById(1L)).thenReturn(sampleResponse);

        ResponseEntity<DocumentMetadataResponse> response = controller.getDocumentById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getFileName()).isEqualTo("report.pdf");
    }

    @Test
    void getDocumentById_delegatesToPreviewService() {
        when(previewService.getById(1L)).thenReturn(sampleResponse);

        controller.getDocumentById(1L);

        verify(previewService, times(1)).getById(1L);
    }

    @Test
    void getDocumentById_notFound_propagatesException() {
        when(previewService.getById(99L)).thenThrow(new RuntimeException("Document not found with id: 99"));

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> controller.getDocumentById(99L));
    }

    // ── failure tests ─────────────────────────────────────────────────────────

    @Test
    void getDocumentViews_serviceThrows_propagatesException() {
        when(documentService.getDocuments(0, 10)).thenThrow(new RuntimeException("DB error"));

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> controller.getDocumentViews(10, 0));
    }

    @Test
    void getAllDocuments_serviceThrows_propagatesException() {
        when(previewService.getAll()).thenThrow(new RuntimeException("Service unavailable"));

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> controller.getAllDocuments());
    }
}
