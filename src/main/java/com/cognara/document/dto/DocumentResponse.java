package com.cognara.document.dto;

import com.cognara.document.Document;

import java.time.Instant;
import java.util.UUID;

/** Safe representation of a document and its tenant/creator metadata. */
public record DocumentResponse(
        UUID id,
        String title,
        String content,
        UUID companyId,
        UUID createdByUserId,
        Instant createdAt,
        Instant updatedAt
) {
    public static DocumentResponse fromEntity(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getTitle(),
                document.getContent(),
                document.getCompany().getId(),
                document.getCreatedByUser().getId(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
