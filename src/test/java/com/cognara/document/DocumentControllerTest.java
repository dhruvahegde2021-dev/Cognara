package com.cognara.document;

import com.cognara.common.exception.GlobalExceptionHandler;
import com.cognara.common.exception.ResourceNotFoundException;
import com.cognara.config.SecurityConfig;
import com.cognara.document.dto.CreateDocumentRequest;
import com.cognara.document.dto.DocumentResponse;
import com.cognara.document.dto.UpdateDocumentRequest;
import com.cognara.document.service.DocumentService;
import com.cognara.security.CustomAuthenticationEntryPoint;
import com.cognara.security.JwtService;
import com.cognara.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DocumentService documentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomAuthenticationEntryPoint authenticationEntryPoint;

    private UsernamePasswordAuthenticationToken authFor(UUID userId, UUID companyId) {
        return new UsernamePasswordAuthenticationToken(
                new UserPrincipal(userId, "user@acme.test", companyId),
                null,
                Collections.emptyList());
    }

    @Test
    @DisplayName("POST /api/documents succeeds with 201 Created")
    void createDocument_withValidRequest_returnsCreated() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        DocumentResponse mockResponse = new DocumentResponse(
                documentId,
                "Policy",
                "Content here",
                companyId,
                userId,
                Instant.now(),
                Instant.now());

        when(documentService.createDocument(eq(companyId), eq(userId), any(CreateDocumentRequest.class)))
                .thenReturn(mockResponse);

        CreateDocumentRequest request = new CreateDocumentRequest("Policy", "Content here");

        mockMvc.perform(post("/api/documents")
                        .with(authentication(authFor(userId, companyId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/documents/" + documentId))
                .andExpect(jsonPath("$.id").value(documentId.toString()))
                .andExpect(jsonPath("$.title").value("Policy"));
    }

    @Test
    @DisplayName("GET /api/documents succeeds with 200 OK and document list")
    void listDocuments_returnsOk() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        DocumentResponse doc = new DocumentResponse(
                UUID.randomUUID(),
                "Doc",
                "Body",
                companyId,
                userId,
                Instant.now(),
                Instant.now());

        when(documentService.listDocuments(companyId)).thenReturn(List.of(doc));

        mockMvc.perform(get("/api/documents").with(authentication(authFor(userId, companyId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Doc"));
    }

    @Test
    @DisplayName("GET /api/documents/{id} fails with 404 when document not found")
    void getDocument_whenNotFound_returnsNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();

        when(documentService.getDocument(companyId, documentId))
                .thenThrow(new ResourceNotFoundException("Document with ID '" + documentId + "' not found"));

        mockMvc.perform(get("/api/documents/{id}", documentId)
                        .with(authentication(authFor(userId, companyId))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Document with ID '" + documentId + "' not found"));
    }

    @Test
    @DisplayName("PUT /api/documents/{id} succeeds with 200 OK")
    void updateDocument_withValidRequest_returnsOk() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        DocumentResponse mockResponse = new DocumentResponse(
                documentId,
                "Updated",
                "New content",
                companyId,
                userId,
                Instant.now(),
                Instant.now());

        when(documentService.updateDocument(eq(companyId), eq(documentId), any(UpdateDocumentRequest.class)))
                .thenReturn(mockResponse);

        UpdateDocumentRequest request = new UpdateDocumentRequest("Updated", "New content");

        mockMvc.perform(put("/api/documents/{id}", documentId)
                        .with(authentication(authFor(userId, companyId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    @DisplayName("DELETE /api/documents/{id} succeeds with 204 No Content")
    void deleteDocument_whenFound_returnsNoContent() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();

        doNothing().when(documentService).deleteDocument(companyId, documentId);

        mockMvc.perform(delete("/api/documents/{id}", documentId)
                        .with(authentication(authFor(userId, companyId))))
                .andExpect(status().isNoContent());
    }
}
