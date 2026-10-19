package com.cognara.company.service;

import com.cognara.common.exception.DuplicateResourceException;
import com.cognara.common.exception.ResourceNotFoundException;
import com.cognara.company.Company;
import com.cognara.company.dto.CompanyResponse;
import com.cognara.company.dto.CreateCompanyRequest;
import com.cognara.company.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service orchestrating company operations and business rules.
 *
 * <p>Spring Annotations Explained:
 * <ul>
 *   <li>{@code @Service}: Marks this class as a Spring-managed service component (business logic layer).
 *       Spring registers it as a Singleton bean in the application context so it can be injected elsewhere.</li>
 *   <li>{@code @Transactional(readOnly = true)}: Defines database transaction boundaries. Placing it at class level
 *       with {@code readOnly = true} optimizes queries (disables dirty-checking overhead in Hibernate).
 *       Write operations override this with {@code @Transactional}.</li>
 * </ul>
 *
 * <p>Constructor Injection:
 * The required {@link CompanyRepository} is injected via constructor, promoting testability and immutability.
 */
@Service
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;

    // Constructor injection: Spring provides the CompanyRepository bean here
    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    /**
     * Creates a new company ensuring slug uniqueness.
     */
    @Transactional
    public CompanyResponse createCompany(CreateCompanyRequest request) {
        String trimmedSlug = request.slug().trim().toLowerCase();

        if (companyRepository.existsBySlug(trimmedSlug)) {
            throw new DuplicateResourceException("A company with slug '" + trimmedSlug + "' already exists");
        }

        Company company = new Company(request.name().trim(), trimmedSlug);
        Company savedCompany = companyRepository.save(company);
        return CompanyResponse.fromEntity(savedCompany);
    }

    /**
     * Retrieves a company by its unique UUID identifier.
     */
    public CompanyResponse getCompanyById(UUID id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company with ID '" + id + "' not found"));
        return CompanyResponse.fromEntity(company);
    }

    /**
     * Retrieves a company by its unique slug identifier.
     */
    public CompanyResponse getCompanyBySlug(String slug) {
        Company company = companyRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Company with slug '" + slug + "' not found"));
        return CompanyResponse.fromEntity(company);
    }
}
