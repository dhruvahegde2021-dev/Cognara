package com.cognara.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a new Company.
 *
 * <p>Validations:
 * <ul>
 *   <li>name: non-blank, max 100 characters</li>
 *   <li>slug: non-blank, 2 to 100 characters, lowercase alphanumeric and hyphens only</li>
 * </ul>
 */
public record CreateCompanyRequest(
        @NotBlank(message = "Company name must not be blank")
        @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Company slug must not be blank")
        @Size(min = 2, max = 100, message = "Company slug must be between 2 and 100 characters")
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Company slug must consist of lowercase letters, numbers, and single hyphens (e.g. 'acme-corp')")
        String slug
) {
}
