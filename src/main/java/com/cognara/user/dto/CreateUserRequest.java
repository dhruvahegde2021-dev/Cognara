package com.cognara.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request payload for creating a new User within a company.
 *
 * <p>Validations:
 * <ul>
 *   <li>name: non-blank, 2 to 100 characters</li>
 *   <li>email: non-blank, valid email syntax, max 150 characters</li>
 *   <li>password: non-blank, min 8 and max 100 characters</li>
 *   <li>companyId: non-null UUID</li>
 * </ul>
 */
public record CreateUserRequest(
        @NotBlank(message = "User name must not be blank")
        @Size(min = 2, max = 100, message = "User name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be a valid email address")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @NotBlank(message = "Password must not be blank")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password,

        @NotNull(message = "Company ID must not be null")
        UUID companyId
) {
}
