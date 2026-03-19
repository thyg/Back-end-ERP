package com.rtcomops.treasury.infrastructure.web.exception;

import com.rtcomops.treasury.application.dto.response.ApiErrorResponse;
import com.rtcomops.treasury.domain.exception.BusinessException;
import com.rtcomops.treasury.domain.exception.DuplicateResourceException;
import com.rtcomops.treasury.domain.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Global exception handler for the Treasury API.
 *
 * <p>Provides centralized exception handling across all controllers,
 * ensuring consistent error response format.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles ResourceNotFoundException.
     *
     * @param ex the exception
     * @param exchange the server web exchange
     * @return Mono containing the error response with 404 status
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleResourceNotFound(
            ResourceNotFoundException ex,
            ServerWebExchange exchange) {

        LOG.warn("Resource not found: {}", ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .error("NOT_FOUND")
            .message(ex.getMessage())
            .path(exchange.getRequest().getPath().value())
            .timestamp(LocalDateTime.now())
            .traceId(UUID.randomUUID().toString())
            .build();

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));
    }

    /**
     * Handles DuplicateResourceException.
     *
     * @param ex the exception
     * @param exchange the server web exchange
     * @return Mono containing the error response with 409 status
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleDuplicateResource(
            DuplicateResourceException ex,
            ServerWebExchange exchange) {

        LOG.warn("Duplicate resource: {}", ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.builder()
            .status(HttpStatus.CONFLICT.value())
            .error("DUPLICATE_RESOURCE")
            .message(ex.getMessage())
            .path(exchange.getRequest().getPath().value())
            .timestamp(LocalDateTime.now())
            .traceId(UUID.randomUUID().toString())
            .build();

        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(response));
    }

    /**
     * Handles BusinessException (business rule violations).
     *
     * @param ex the exception
     * @param exchange the server web exchange
     * @return Mono containing the error response with 400 status
     */
    @ExceptionHandler(BusinessException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleBusinessException(
            BusinessException ex,
            ServerWebExchange exchange) {

        LOG.warn("Business rule violation: {}", ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("BUSINESS_ERROR")
            .message(ex.getMessage())
            .path(exchange.getRequest().getPath().value())
            .timestamp(LocalDateTime.now())
            .traceId(UUID.randomUUID().toString())
            .build();

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    /**
     * Handles validation errors from request body.
     *
     * @param ex the validation exception
     * @param exchange the server web exchange
     * @return Mono containing the error response with 400 status
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleValidationErrors(
            WebExchangeBindException ex,
            ServerWebExchange exchange) {

        LOG.warn("Validation error on request to {}", exchange.getRequest().getPath().value());

        Map<String, List<String>> validationErrors = new HashMap<>();

        for (FieldError fieldError : ex.getFieldErrors()) {
            String fieldName = fieldError.getField();
            String errorMessage = fieldError.getDefaultMessage();
            LOG.warn("  - Field '{}': {} (rejected value: {})", fieldName, errorMessage, fieldError.getRejectedValue());
            validationErrors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMessage);
        }

        ApiErrorResponse response = ApiErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("VALIDATION_ERROR")
            .message("Request validation failed")
            .path(exchange.getRequest().getPath().value())
            .timestamp(LocalDateTime.now())
            .validationErrors(validationErrors)
            .traceId(UUID.randomUUID().toString())
            .build();

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    /**
     * Handles all other unexpected exceptions.
     *
     * @param ex the exception
     * @param exchange the server web exchange
     * @return Mono containing the error response with 500 status
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleGenericException(
            Exception ex,
            ServerWebExchange exchange) {

        String traceId = UUID.randomUUID().toString();
        LOG.error("Unexpected error [traceId={}]: {}", traceId, ex.getMessage(), ex);

        ApiErrorResponse response = ApiErrorResponse.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("INTERNAL_ERROR")
            .message("An unexpected error occurred. Please contact support with trace ID: " + traceId)
            .path(exchange.getRequest().getPath().value())
            .timestamp(LocalDateTime.now())
            .traceId(traceId)
            .build();

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }
}
