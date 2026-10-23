package com.cognara.document.service;

import com.cognara.common.exception.ResourceNotFoundException;
import com.cognara.company.Company;
import com.cognara.company.repository.CompanyRepository;
import com.cognara.document.Document;
import com.cognara.document.dto.CreateDocumentRequest;
import com.cognara.document.dto.DocumentResponse;
import com.cognara.document.repository.DocumentRepository;
import com.cognara.user.User;
import com.cognara.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DocumentService documentService;

    @Test
    void createDocument_withTenantCreator_savesAndReturnsDocument() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Company company = company(companyId);
        User creator = user(userId, company);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(userRepository.findByIdAndCompanyId(userId, companyId)).thenReturn(Optional.of(creator));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document document = invocation.getArgument(0);
            document.setId(UUID.randomUUID());
            return document;
        });

        DocumentResponse response = documentService.createDocument(
                companyId, userId, new CreateDocumentRequest("  Support guide  ", "Document content"));

        assertThat(response.title()).isEqualTo("Support guide");
        assertThat(response.content()).isEqualTo("Document content");
        assertThat(response.companyId()).isEqualTo(companyId);
        assertThat(response.createdByUserId()).isEqualTo(userId);
        ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(documentCaptor.capture());
        assertThat(documentCaptor.getValue().getCompany()).isSameAs(company);
        assertThat(documentCaptor.getValue().getCreatedByUser()).isSameAs(creator);
    }

    @Test
    void getDocument_fromAnotherCompany_throwsNotFound() {
        UUID requestedCompanyId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        when(documentRepository.findByIdAndCompanyId(documentId, requestedCompanyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getDocument(requestedCompanyId, documentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(documentId.toString());
    }

    @Test
    void createDocument_whenCreatorDoesNotBelongToCompany_throwsNotFound() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company(companyId)));
        when(userRepository.findByIdAndCompanyId(userId, companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.createDocument(
                companyId, userId, new CreateDocumentRequest("Support guide", "Document content")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(userId.toString())
                .hasMessageContaining(companyId.toString());

        verify(documentRepository, never()).save(any(Document.class));
    }

    private Company company(UUID id) {
        Company company = new Company("Acme", "acme");
        company.setId(id);
        return company;
    }

    private User user(UUID id, Company company) {
        User user = new User("Ada", "ada@acme.test", "hash", company);
        user.setId(id);
        return user;
    }
}
