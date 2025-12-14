package com.rtcomops.treasury.exception;

/**
 * Exception thrown when attempting to create a resource that already exists.
 *
 * <p>This exception should be thrown when a unique constraint
 * would be violated (e.g., duplicate code).</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
public class DuplicateResourceException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Resource already exists";

    /**
     * Creates a new DuplicateResourceException with a default message.
     */
    public DuplicateResourceException() {
        super(DEFAULT_MESSAGE);
    }

    /**
     * Creates a new DuplicateResourceException with a custom message.
     *
     * @param message the error message
     */
    public DuplicateResourceException(String message) {
        super(message);
    }

    /**
     * Creates a new DuplicateResourceException for a specific resource and field.
     *
     * @param resourceName the name of the resource type
     * @param fieldName the field that has a duplicate value
     * @param fieldValue the duplicate value
     */
    public DuplicateResourceException(String resourceName, String fieldName, String fieldValue) {
        super(String.format("%s already exists with %s: %s", resourceName, fieldName, fieldValue));
    }
}
