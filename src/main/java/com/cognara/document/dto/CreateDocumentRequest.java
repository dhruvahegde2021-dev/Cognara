package com.cognara.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request payload for creating a tenant-scoped text document. */
public record CreateDocumentRequest(
        @NotBlank(message = "Document title must not be blank")
        @Size(max = 255, message = "Document title must not exceed 255 characters")
        String title,

        @NotBlank(message = "Document content must not be blank")
        String content
) {
}
