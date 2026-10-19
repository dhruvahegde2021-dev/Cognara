package com.cognara.company.service;

import com.cognara.common.exception.DuplicateResourceException;
import com.cognara.common.exception.ResourceNotFoundException;
import com.cognara.company.Company;
import com.cognara.company.dto.CompanyResponse;
import com.cognara.company.dto.CreateCompanyRequest;
import com.cognara.company.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CompanyService business logic and validation rules.
 */
@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyService companyService;

    private CreateCompanyRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = new CreateCompanyRequest("Tech Innovators", "tech-innovators");
    }

    @Test
    @DisplayName("createCompany successfully saves and maps company when slug is unique")
    void createCompany_withUniqueSlug_savesAndReturnsCompany() {
        when(companyRepository.existsBySlug("tech-innovators")).thenReturn(false);

        Company savedCompany = new Company("Tech Innovators", "tech-innovators");
        UUID expectedId = UUID.randomUUID();
        savedCompany.setId(expectedId);

        when(companyRepository.save(any(Company.class))).thenReturn(savedCompany);

        CompanyResponse response = companyService.createCompany(createRequest);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(expectedId);
        assertThat(response.name()).isEqualTo("Tech Innovators");
        assertThat(response.slug()).isEqualTo("tech-innovators");

        verify(companyRepository).existsBySlug("tech-innovators");
        verify(companyRepository).save(any(Company.class));
    }

    @Test
    @DisplayName("createCompany throws DuplicateResourceException when slug already exists")
    void createCompany_withExistingSlug_throwsDuplicateResourceException() {
        when(companyRepository.existsBySlug("tech-innovators")).thenReturn(true);

        assertThatThrownBy(() -> companyService.createCompany(createRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("tech-innovators");

        verify(companyRepository).existsBySlug("tech-innovators");
        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    @DisplayName("getCompanyById returns CompanyResponse when company exists")
    void getCompanyById_whenFound_returnsCompanyResponse() {
        UUID companyId = UUID.randomUUID();
        Company company = new Company("Tech Innovators", "tech-innovators");
        company.setId(companyId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        CompanyResponse response = companyService.getCompanyById(companyId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(companyId);
        assertThat(response.slug()).isEqualTo("tech-innovators");
    }

    @Test
    @DisplayName("getCompanyById throws ResourceNotFoundException when company is not found")
    void getCompanyById_whenNotFound_throwsResourceNotFoundException() {
        UUID companyId = UUID.randomUUID();
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.getCompanyById(companyId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(companyId.toString());
    }
}
