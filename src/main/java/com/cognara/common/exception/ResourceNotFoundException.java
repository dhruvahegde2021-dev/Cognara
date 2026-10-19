package com.cognara.common.exception;

/**
 * Thrown when a resource requested by ID or unique key cannot be found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
