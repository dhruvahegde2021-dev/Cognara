package com.cognara.document.service;

import com.cognara.common.exception.ResourceNotFoundException;
import com.cognara.company.Company;
import com.cognara.company.repository.CompanyRepository;
import com.cognara.document.Document;
import com.cognara.document.dto.CreateDocumentRequest;
import com.cognara.document.dto.DocumentResponse;
import com.cognara.document.dto.UpdateDocumentRequest;
import com.cognara.document.repository.DocumentRepository;
import com.cognara.user.User;
import com.cognara.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Service layer for tenant-scoped document persistence operations. */
@Service
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public DocumentService(
            DocumentRepository documentRepository,
            CompanyRepository companyRepository,
            UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public DocumentResponse createDocument(UUID companyId, UUID userId, CreateDocumentRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> companyNotFound(companyId));
        User creator = userRepository.findByIdAndCompanyId(userId, companyId)
                .orElseThrow(() -> creatorNotFound(userId, companyId));

        Document document = new Document(request.title().trim(), request.content(), company, creator);
        return DocumentResponse.fromEntity(documentRepository.save(document));
    }

    public List<DocumentResponse> listDocuments(UUID companyId) {
        return documentRepository.findByCompanyIdOrderByCreatedAtDesc(companyId).stream()
                .map(DocumentResponse::fromEntity)
                .toList();
    }

    public DocumentResponse getDocument(UUID companyId, UUID documentId) {
        return DocumentResponse.fromEntity(findDocumentInCompany(documentId, companyId));
    }

    @Transactional
    public DocumentResponse updateDocument(UUID companyId, UUID documentId, UpdateDocumentRequest request) {
        Document document = findDocumentInCompany(documentId, companyId);
        document.setTitle(request.title().trim());
        document.setContent(request.content());
        return DocumentResponse.fromEntity(documentRepository.save(document));
    }

    @Transactional
    public void deleteDocument(UUID companyId, UUID documentId) {
        documentRepository.delete(findDocumentInCompany(documentId, companyId));
    }

    private Document findDocumentInCompany(UUID documentId, UUID companyId) {
        return documentRepository.findByIdAndCompanyId(documentId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document with ID '" + documentId + "' not found"));
    }

    private ResourceNotFoundException companyNotFound(UUID companyId) {
        return new ResourceNotFoundException("Company with ID '" + companyId + "' not found");
    }

    private ResourceNotFoundException creatorNotFound(UUID userId, UUID companyId) {
        return new ResourceNotFoundException(
                "User with ID '" + userId + "' not found in company '" + companyId + "'");
    }
}
