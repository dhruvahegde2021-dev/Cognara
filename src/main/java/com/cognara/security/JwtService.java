package com.cognara.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Service for issuing and validating signed JSON Web Tokens (JWT).
 *
 * <p>Embeds:
 * <ul>
 *   <li>{@code sub}: User UUID</li>
 *   <li>{@code email}: User email</li>
 *   <li>{@code companyId}: Owning company UUID (for multi-tenant scoping)</li>
 * </ul>
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${cognara.security.jwt.secret}") String secret,
            @Value("${cognara.security.jwt.expiration-ms}") long expirationMs) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("The configured JWT signing secret must be at least 32 bytes");
        }
        if (expirationMs <= 0) {
            throw new IllegalStateException("JWT_EXPIRATION_MS must be positive");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a signed JWT with standard and custom claims.
     */
    public String generateToken(UUID userId, String email, UUID companyId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("companyId", companyId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Parses and verifies signature and expiration of the token.
     * Returns extracted claims if valid, or null if invalid/expired.
     */
    public Claims parseAndValidateClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Extracts UserPrincipal from validated claims.
     */
    public UserPrincipal extractUserPrincipal(Claims claims) {
        if (claims == null) {
            return null;
        }
        try {
            UUID userId = UUID.fromString(claims.getSubject());
            String email = claims.get("email", String.class);
            String companyIdStr = claims.get("companyId", String.class);
            if (email == null || email.isBlank() || companyIdStr == null) {
                return null;
            }
            UUID companyId = UUID.fromString(companyIdStr);

            return new UserPrincipal(userId, email, companyId);
        } catch (Exception e) {
            return null;
        }
    }
}
