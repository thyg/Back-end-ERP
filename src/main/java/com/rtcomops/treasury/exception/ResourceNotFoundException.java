package com.rtcomops.treasury.exception;

import java.util.UUID;

/**
 * Exception thrown when a requested resource is not found.
 *
 * <p>This exception should be thrown when a database lookup
 * returns no results for the given identifier.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
public class ResourceNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Resource not found";

    /**
     * Creates a new ResourceNotFoundException with a default message.
     */
    public ResourceNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    /**
     * Creates a new ResourceNotFoundException with a custom message.
     *
     * @param message the error message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a new ResourceNotFoundException for a specific resource type and ID.
     *
     * @param resourceName the name of the resource type (e.g., "Bank")
     * @param id the ID that was not found
     */
    public ResourceNotFoundException(String resourceName, UUID id) {
        super(String.format("%s not found with id: %s", resourceName, id));
    }

    /**
     * Creates a new ResourceNotFoundException for a specific resource type and field.
     *
     * @param resourceName the name of the resource type
     * @param fieldName the field that was searched
     * @param fieldValue the value that was not found
     */
    public ResourceNotFoundException(String resourceName, String fieldName, String fieldValue) {
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue));
    }
}
