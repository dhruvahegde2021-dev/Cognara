package com.cognara.common.exception;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Standard error response structure for API clients.
 *
 * @param status    HTTP status code (e.g. 400, 404, 409)
 * @param error     HTTP status error phrase
 * @param message   Human-readable summary of the error
 * @param path      Request URI path
 * @param timestamp Time of occurrence
 * @param details   Optional map or list of field validation errors
 */
public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp,
        Object details
) {
    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(status, error, message, path, Instant.now(), null);
    }

    public static ApiErrorResponse of(int status, String error, String message, String path, Object details) {
        return new ApiErrorResponse(status, error, message, path, Instant.now(), details);
    }
}
