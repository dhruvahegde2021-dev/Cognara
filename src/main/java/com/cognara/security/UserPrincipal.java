package com.cognara.security;

import java.util.UUID;

/**
 * Custom authentication token holding verified authenticated user details
 * and their associated tenant organization (company).
 */
public record UserPrincipal(
        UUID userId,
        String email,
        UUID companyId
) {
}
