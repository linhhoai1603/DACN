package com.example.documentmanagementbackend.versionControlListTest;

import com.example.documentmanagementbackend.dto.response.DocumentMetadataResponse;
import com.example.documentmanagementbackend.model.DocumentMetadata;
import com.example.documentmanagementbackend.repository.DocumentMetadataRepository;
import com.example.documentmanagementbackend.service.DocumentPreviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentPreviewServiceTest {

    @Mock
    private DocumentMetadataRepository metadataRepository;

    @InjectMocks
    private DocumentPreviewService previewService;

    private DocumentMetadata pdfDoc;
    private DocumentMetadata docxDoc;
    private DocumentMetadata xlsxDoc;
    private DocumentMetadata unknownDoc;

    @BeforeEach
    void setUp() {
        pdfDoc = DocumentMetadata.builder()
                .id(1L).fileName("report.pdf").publicId("pub1")
                .url("http://cdn.example.com/report.pdf")
                .fileSize(1024L).uploadedBy("alice")
                .uploadedAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .commitMessage("initial").version("v1").build();

        docxDoc = DocumentMetadata.builder()
                .id(2L).fileName("letter.docx").publicId("pub2")
                .url("http://cdn.example.com/letter.docx")
                .fileSize(2048L).uploadedBy("bob")
                .uploadedAt(LocalDateTime.of(2024, 2, 1, 9, 0))
                .commitMessage("draft").version("v1").build();

        xlsxDoc = DocumentMetadata.builder()
                .id(3L).fileName("data.xlsx").publicId("pub3")
                .url("http://cdn.example.com/data.xlsx")
                .fileSize(512L).uploadedBy("carol")
                .uploadedAt(LocalDateTime.of(2024, 3, 1, 8, 0))
                .commitMessage("sheet").version("v2").build();

        unknownDoc = DocumentMetadata.builder()
                .id(4L).fileName("archive.zip").publicId("pub4")
                .url("http://cdn.example.com/archive.zip")
                .fileSize(4096L).uploadedBy("dave")
                .uploadedAt(LocalDateTime.of(2024, 4, 1, 7, 0))
                .commitMessage("zip").version("v1").build();
    }

    // ── getById ──────────────────────────────────────────────────────────────

    @Test
    void getById_pdf_returnsStreamUrlAsPreview() {
        when(metadataRepository.findById(1L)).thenReturn(Optional.of(pdfDoc));

        DocumentMetadataResponse res = previewService.getById(1L);

        assertThat(res.getId()).isEqualTo(1L);
        assertThat(res.getFileType()).isEqualTo("pdf");
        assertThat(res.getPreviewUrl()).isEqualTo("http://localhost:8080/files/1/stream");
    }

    @Test
    void getById_docx_returnsGoogleViewerUrl() {
        when(metadataRepository.findById(2L)).thenReturn(Optional.of(docxDoc));

        DocumentMetadataResponse res = previewService.getById(2L);

        assertThat(res.getFileType()).isEqualTo("docx");
        assertThat(res.getPreviewUrl()).startsWith("https://docs.google.com/viewer?embedded=true&url=");
        assertThat(res.getPreviewUrl()).contains("localhost%3A8080");
    }

    @Test
    void getById_xlsx_returnsGoogleViewerUrl() {
        when(metadataRepository.findById(3L)).thenReturn(Optional.of(xlsxDoc));

        DocumentMetadataResponse res = previewService.getById(3L);

        assertThat(res.getFileType()).isEqualTo("xlsx");
        assertThat(res.getPreviewUrl()).startsWith("https://docs.google.com/viewer?embedded=true&url=");
    }

    @Test
    void getById_unknownExtension_returnsStreamUrl() {
        when(metadataRepository.findById(4L)).thenReturn(Optional.of(unknownDoc));

        DocumentMetadataResponse res = previewService.getById(4L);

        assertThat(res.getFileType()).isEqualTo("zip");
        assertThat(res.getPreviewUrl()).isEqualTo("http://localhost:8080/files/4/stream");
    }

    @Test
    void getById_notFound_throwsRuntimeException() {
        when(metadataRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> previewService.getById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getById_mapsAllFieldsCorrectly() {
        when(metadataRepository.findById(1L)).thenReturn(Optional.of(pdfDoc));

        DocumentMetadataResponse res = previewService.getById(1L);

        assertThat(res.getFileName()).isEqualTo("report.pdf");
        assertThat(res.getPublicId()).isEqualTo("pub1");
        assertThat(res.getUrl()).isEqualTo("http://cdn.example.com/report.pdf");
        assertThat(res.getFileSize()).isEqualTo(1024L);
        assertThat(res.getUploadedBy()).isEqualTo("alice");
        assertThat(res.getCommitMessage()).isEqualTo("initial");
        assertThat(res.getVersion()).isEqualTo("v1");
    }

    // ── getAll ───────────────────────────────────────────────────────────────

    @Test
    void getAll_returnsAllDocuments() {
        when(metadataRepository.findAll()).thenReturn(List.of(pdfDoc, docxDoc, xlsxDoc));

        List<DocumentMetadataResponse> result = previewService.getAll();

        assertThat(result).hasSize(3);
        assertThat(result).extracting(DocumentMetadataResponse::getId)
                .containsExactly(1L, 2L, 3L);
    }

    @Test
    void getAll_emptyRepository_returnsEmptyList() {
        when(metadataRepository.findAll()).thenReturn(List.of());

        List<DocumentMetadataResponse> result = previewService.getAll();

        assertThat(result).isEmpty();
    }

    @Test
    void getAll_eachDocHasCorrectPreviewUrl() {
        when(metadataRepository.findAll()).thenReturn(List.of(pdfDoc, docxDoc));

        List<DocumentMetadataResponse> result = previewService.getAll();

        // pdf → stream url
        assertThat(result.get(0).getPreviewUrl()).isEqualTo("http://localhost:8080/files/1/stream");
        // docx → google viewer
        assertThat(result.get(1).getPreviewUrl()).startsWith("https://docs.google.com/viewer");
    }

    // ── extension edge cases ─────────────────────────────────────────────────

    @Test
    void getById_docExtension_returnsGoogleViewerUrl() {
        DocumentMetadata doc = DocumentMetadata.builder()
                .id(5L).fileName("memo.doc").publicId("pub5")
                .url("http://cdn.example.com/memo.doc")
                .fileSize(100L).uploadedBy("eve")
                .uploadedAt(LocalDateTime.now())
                .commitMessage("memo").version("v1").build();
        when(metadataRepository.findById(5L)).thenReturn(Optional.of(doc));

        DocumentMetadataResponse res = previewService.getById(5L);

        assertThat(res.getFileType()).isEqualTo("doc");
        assertThat(res.getPreviewUrl()).startsWith("https://docs.google.com/viewer");
    }

    @Test
    void getById_xlsExtension_returnsGoogleViewerUrl() {
        DocumentMetadata doc = DocumentMetadata.builder()
                .id(6L).fileName("sheet.xls").publicId("pub6")
                .url("http://cdn.example.com/sheet.xls")
                .fileSize(200L).uploadedBy("frank")
                .uploadedAt(LocalDateTime.now())
                .commitMessage("xls").version("v1").build();
        when(metadataRepository.findById(6L)).thenReturn(Optional.of(doc));

        DocumentMetadataResponse res = previewService.getById(6L);

        assertThat(res.getFileType()).isEqualTo("xls");
        assertThat(res.getPreviewUrl()).startsWith("https://docs.google.com/viewer");
    }

    @Test
    void getById_fileNameWithNoExtension_returnsEmptyFileType() {
        DocumentMetadata doc = DocumentMetadata.builder()
                .id(7L).fileName("README").publicId("pub7")
                .url("http://cdn.example.com/README")
                .fileSize(50L).uploadedBy("grace")
                .uploadedAt(LocalDateTime.now())
                .commitMessage("readme").version("v1").build();
        when(metadataRepository.findById(7L)).thenReturn(Optional.of(doc));

        DocumentMetadataResponse res = previewService.getById(7L);

        assertThat(res.getFileType()).isEmpty();
        assertThat(res.getPreviewUrl()).isEqualTo("http://localhost:8080/files/7/stream");
    }

    @Test
    void getById_fileNameUpperCaseExtension_isNormalizedToLower() {
        DocumentMetadata doc = DocumentMetadata.builder()
                .id(8L).fileName("REPORT.PDF").publicId("pub8")
                .url("http://cdn.example.com/REPORT.PDF")
                .fileSize(300L).uploadedBy("henry")
                .uploadedAt(LocalDateTime.now())
                .commitMessage("upper").version("v1").build();
        when(metadataRepository.findById(8L)).thenReturn(Optional.of(doc));

        DocumentMetadataResponse res = previewService.getById(8L);

        assertThat(res.getFileType()).isEqualTo("pdf");
        assertThat(res.getPreviewUrl()).isEqualTo("http://localhost:8080/files/8/stream");
    }

    // ── failure tests ─────────────────────────────────────────────────────────

    @Test
    void getById_nullFileName_throwsException() {
        DocumentMetadata doc = DocumentMetadata.builder()
                .id(10L).fileName(null).publicId("pub10")
                .url("http://cdn.example.com/file")
                .fileSize(100L).uploadedBy("user")
                .uploadedAt(LocalDateTime.now())
                .commitMessage("null name").version("v1").build();
        when(metadataRepository.findById(10L)).thenReturn(Optional.of(doc));

        assertThatThrownBy(() -> previewService.getById(10L))
                .isInstanceOf(Exception.class);
    }

    @Test
    void getAll_repositoryThrows_propagatesException() {
        when(metadataRepository.findAll()).thenThrow(new RuntimeException("DB connection failed"));

        assertThatThrownBy(() -> previewService.getAll())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB connection failed");
    }
}
