package com.cognara.document;

import com.cognara.company.Company;
import com.cognara.company.repository.CompanyRepository;
import com.cognara.document.repository.DocumentRepository;
import com.cognara.user.User;
import com.cognara.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class DocumentRepositoryTest {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void tenantScopedLookup_findsOwnDocumentButNotAnotherCompanysDocument() {
        Company companyA = companyRepository.save(new Company("Company A", "company-a"));
        Company companyB = companyRepository.save(new Company("Company B", "company-b"));
        User userA = userRepository.save(new User("Alice", "alice@company-a.test", "hash", companyA));
        User userB = userRepository.save(new User("Bob", "bob@company-b.test", "hash", companyB));

        Document documentA = documentRepository.saveAndFlush(
                new Document("Company A guide", "Internal guidance", companyA, userA));
        documentRepository.saveAndFlush(
                new Document("Company B guide", "Other tenant guidance", companyB, userB));

        Optional<Document> ownDocument = documentRepository.findByIdAndCompanyId(documentA.getId(), companyA.getId());
        Optional<Document> crossTenantDocument = documentRepository.findByIdAndCompanyId(documentA.getId(), companyB.getId());

        assertThat(ownDocument).isPresent();
        assertThat(ownDocument.get().getCompany().getId()).isEqualTo(companyA.getId());
        assertThat(crossTenantDocument).isEmpty();
    }
}
