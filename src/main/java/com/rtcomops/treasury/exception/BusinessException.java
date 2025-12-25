package com.rtcomops.treasury.exception;

/**
 * Exception thrown when a business rule is violated.
 *
 * <p>This exception should be thrown when a business validation fails
 * (e.g., invalid state transition, constraint violation).</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
public class BusinessException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Business rule violation";

    /**
     * Creates a new BusinessException with a default message.
     */
    public BusinessException() {
        super(DEFAULT_MESSAGE);
    }

    /**
     * Creates a new BusinessException with a custom message.
     *
     * @param message the error message
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * Creates a new BusinessException with a custom message and cause.
     *
     * @param message the error message
     * @param cause the underlying cause
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
