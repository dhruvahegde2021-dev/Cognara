package com.cognara.document;

import com.cognara.document.dto.CreateDocumentRequest;
import com.cognara.document.dto.DocumentResponse;
import com.cognara.document.dto.UpdateDocumentRequest;
import com.cognara.document.service.DocumentService;
import com.cognara.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/** REST controller for tenant-scoped document management. */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * POST /api/documents
     * Creates a document for the authenticated user's company.
     */
    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateDocumentRequest request) {
        DocumentResponse response = documentService.createDocument(
                principal.companyId(), principal.userId(), request);
        URI location = URI.create("/api/documents/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    /**
     * GET /api/documents
     * Lists documents belonging to the authenticated user's company.
     */
    @GetMapping
    public ResponseEntity<List<DocumentResponse>> listDocuments(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<DocumentResponse> documents = documentService.listDocuments(principal.companyId());
        return ResponseEntity.ok(documents);
    }

    /**
     * GET /api/documents/{id}
     * Retrieves a document by ID within the authenticated user's company.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        DocumentResponse response = documentService.getDocument(principal.companyId(), id);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/documents/{id}
     * Updates a document within the authenticated user's company.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponse> updateDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDocumentRequest request) {
        DocumentResponse response = documentService.updateDocument(principal.companyId(), id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/documents/{id}
     * Deletes a document within the authenticated user's company.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        documentService.deleteDocument(principal.companyId(), id);
        return ResponseEntity.noContent().build();
    }
}
