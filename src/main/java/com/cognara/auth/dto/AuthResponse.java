package com.cognara.auth.dto;

import java.util.UUID;

/**
 * Successful authentication response containing JWT and user details.
 */
public record AuthResponse(
        String token,
        String tokenType,
        UUID userId,
        String name,
        String email,
        UUID companyId,
        String companyName
) {
    public static AuthResponse of(String token, UUID userId, String name, String email, UUID companyId, String companyName) {
        return new AuthResponse(token, "Bearer", userId, name, email, companyId, companyName);
    }
}
