package com.rtcomops.treasury.application.port.in;

import com.rtcomops.treasury.application.dto.request.AuditLogFilterRequest;
import com.rtcomops.treasury.application.dto.response.AuditLogResponse;
import com.rtcomops.treasury.domain.model.enums.AuditAction;
import com.rtcomops.treasury.domain.model.enums.AuditModule;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Input port for AuditLog use cases.
 *
 * <p>Defines the contract for audit log business operations.
 * This port is implemented by the domain service and used by
 * both the infrastructure layer and other domain services.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-13
 */
public interface AuditLogUseCase {

    // =========================================================================
    // LOGGING METHODS (Called by other services)
    // =========================================================================

    /**
     * Logs an audit entry.
     *
     * <p>This is the main method called by other services to record actions.</p>
     *
     * @param module The module where the action occurred
     * @param action The action performed
     * @param entityId The ID of the affected entity
     * @param entityReference Human-readable reference
     * @param oldValue The entity state before the change (null for CREATE)
     * @param newValue The entity state after the change (null for DELETE)
     * @param description Human-readable description
     * @return Mono<Void> completes when the log is saved
     */
    Mono<Void> log(
            AuditModule module,
            AuditAction action,
            UUID entityId,
            String entityReference,
            Object oldValue,
            Object newValue,
            String description);

    /**
     * Logs an audit entry with user context.
     *
     * @param module The module where the action occurred
     * @param action The action performed
     * @param entityId The ID of the affected entity
     * @param entityReference Human-readable reference
     * @param oldValue The entity state before the change
     * @param newValue The entity state after the change
     * @param description Human-readable description
     * @param userId The user ID
     * @param userName The user name
     * @param ipAddress The IP address
     * @param userAgent The user agent
     * @return Mono<Void> completes when the log is saved
     */
    Mono<Void> log(
            AuditModule module,
            AuditAction action,
            UUID entityId,
            String entityReference,
            Object oldValue,
            Object newValue,
            String description,
            UUID userId,
            String userName,
            String ipAddress,
            String userAgent);

    /**
     * Simplified log method for CREATE operations.
     *
     * @param module The module
     * @param entityId The entity ID
     * @param reference The entity reference
     * @param entity The created entity
     * @param description The description
     * @return Mono<Void>
     */
    Mono<Void> logCreate(AuditModule module, UUID entityId, String reference, Object entity, String description);

    /**
     * Simplified log method for UPDATE operations.
     *
     * @param module The module
     * @param entityId The entity ID
     * @param reference The entity reference
     * @param oldEntity The old entity state
     * @param newEntity The new entity state
     * @param description The description
     * @return Mono<Void>
     */
    Mono<Void> logUpdate(AuditModule module, UUID entityId, String reference, Object oldEntity, Object newEntity, String description);

    /**
     * Simplified log method for DELETE operations.
     *
     * @param module The module
     * @param entityId The entity ID
     * @param reference The entity reference
     * @param entity The deleted entity
     * @param description The description
     * @return Mono<Void>
     */
    Mono<Void> logDelete(AuditModule module, UUID entityId, String reference, Object entity, String description);

    // =========================================================================
    // QUERY METHODS
    // =========================================================================

    /**
     * Finds audit logs with filters.
     *
     * @param filter The filter criteria
     * @return Flux of AuditLogResponse DTOs
     */
    Flux<AuditLogResponse> findWithFilters(AuditLogFilterRequest filter);

    /**
     * Counts audit logs with filters.
     *
     * @param filter The filter criteria
     * @return Mono containing the count
     */
    Mono<Long> countWithFilters(AuditLogFilterRequest filter);

    /**
     * Finds audit log by ID.
     *
     * @param id The audit log ID
     * @return Mono of AuditLogResponse
     */
    Mono<AuditLogResponse> findById(UUID id);

    /**
     * Finds all audit logs for a specific entity (history).
     *
     * @param entityId The entity ID
     * @return Flux of AuditLogResponse DTOs
     */
    Flux<AuditLogResponse> findByEntityId(UUID entityId);

    /**
     * Finds today's audit logs.
     *
     * @return Flux of AuditLogResponse DTOs
     */
    Flux<AuditLogResponse> findToday();

    // =========================================================================
    // CLEANUP
    // =========================================================================

    /**
     * Deletes audit logs older than the specified number of days.
     *
     * @param daysToKeep Number of days to keep
     * @return Mono containing the number of deleted records
     */
    Mono<Long> cleanup(int daysToKeep);
}
