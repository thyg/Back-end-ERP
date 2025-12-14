package com.rtcomops.treasury.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Standard API error response format.
 *
 * <p>Provides a consistent error response structure across all endpoints,
 * making it easier for clients to handle errors.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    /**
     * HTTP status code.
     */
    private int status;

    /**
     * Error type/code for programmatic handling.
     */
    private String error;

    /**
     * Human-readable error message.
     */
    private String message;

    /**
     * Request path that caused the error.
     */
    private String path;

    /**
     * Timestamp when the error occurred.
     */
    private LocalDateTime timestamp;

    /**
     * Detailed validation errors (field -> list of error messages).
     */
    private Map<String, List<String>> validationErrors;

    /**
     * Trace ID for debugging (optional).
     */
    private String traceId;
}
