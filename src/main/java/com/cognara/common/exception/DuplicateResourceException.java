package com.cognara.common.exception;

/**
 * Thrown when an operation violates uniqueness or business conflict rules
 * (e.g., attempting to create a company with a duplicate slug).
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
