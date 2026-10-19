package com.cognara.company.dto;

import com.cognara.company.Company;

import java.time.Instant;
import java.util.UUID;

/**
 * Response payload returning public company details.
 *
 * <p>Exposes only safe, intended fields. Sensitive internal fields or credentials
 * (like user password hashes) are never included.
 */
public record CompanyResponse(
        UUID id,
        String name,
        String slug,
        Instant createdAt
) {
    public static CompanyResponse fromEntity(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getSlug(),
                company.getCreatedAt()
        );
    }
}
