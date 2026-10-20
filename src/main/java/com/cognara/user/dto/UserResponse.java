package com.cognara.user.dto;

import com.cognara.user.User;

import java.util.UUID;

/**
 * Safe public representation of a User.
 *
 * <p>Never exposes credentials or password hashes.
 */
public record UserResponse(
        UUID id,
        String name,
        String email,
        UUID companyId,
        String companyName
) {
    public static UserResponse fromEntity(User user) {
        UUID companyId = user.getCompany() != null ? user.getCompany().getId() : null;
        String companyName = user.getCompany() != null ? user.getCompany().getName() : null;

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                companyId,
                companyName
        );
    }
}
