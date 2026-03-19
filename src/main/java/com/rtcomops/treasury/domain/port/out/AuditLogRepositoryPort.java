package com.rtcomops.treasury.domain.port.out;

import com.rtcomops.treasury.domain.model.AuditLog;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Output port for AuditLog repository operations.
 *
 * <p>Defines the contract for persistence operations on AuditLog entities.
 * This port is implemented by the infrastructure layer adapter.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-13
 */
public interface AuditLogRepositoryPort {

    /**
     * Finds an audit log by its unique identifier.
     *
     * @param id the audit log ID
     * @return Mono containing the audit log if found
     */
    Mono<AuditLog> findById(UUID id);

    /**
     * Finds all audit logs ordered by creation date (newest first).
     *
     * @param limit maximum number of results
     * @param offset number of results to skip
     * @return Flux of audit logs
     */
    Flux<AuditLog> findAllPaginated(int limit, long offset);

    /**
     * Counts total audit logs.
     *
     * @return Mono containing the count
     */
    Mono<Long> countAll();

    /**
     * Finds audit logs by module.
     *
     * @param module the module name
     * @param limit maximum number of results
     * @param offset number of results to skip
     * @return Flux of audit logs
     */
    Flux<AuditLog> findByModule(String module, int limit, long offset);

    /**
     * Counts audit logs by module.
     *
     * @param module the module name
     * @return Mono containing the count
     */
    Mono<Long> countByModule(String module);

    /**
     * Finds audit logs by action.
     *
     * @param action the action name
     * @param limit maximum number of results
     * @param offset number of results to skip
     * @return Flux of audit logs
     */
    Flux<AuditLog> findByAction(String action, int limit, long offset);

    /**
     * Counts audit logs by action.
     *
     * @param action the action name
     * @return Mono containing the count
     */
    Mono<Long> countByAction(String action);

    /**
     * Finds audit logs for a specific entity.
     *
     * @param entityId the entity ID
     * @return Flux of audit logs
     */
    Flux<AuditLog> findByEntityId(UUID entityId);

    /**
     * Finds audit logs by user ID.
     *
     * @param userId the user ID
     * @param limit maximum number of results
     * @param offset number of results to skip
     * @return Flux of audit logs
     */
    Flux<AuditLog> findByUserId(UUID userId, int limit, long offset);

    /**
     * Finds audit logs within a date range.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @param limit maximum number of results
     * @param offset number of results to skip
     * @return Flux of audit logs
     */
    Flux<AuditLog> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, int limit, long offset);

    /**
     * Counts audit logs within a date range.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return Mono containing the count
     */
    Mono<Long> countByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds audit logs from today.
     *
     * @return Flux of today's audit logs
     */
    Flux<AuditLog> findToday();

    /**
     * Finds audit logs by module and action.
     *
     * @param module the module name
     * @param action the action name
     * @param limit maximum number of results
     * @param offset number of results to skip
     * @return Flux of audit logs
     */
    Flux<AuditLog> findByModuleAndAction(String module, String action, int limit, long offset);

    /**
     * Finds audit logs by module within date range.
     *
     * @param module the module name
     * @param startDate the start date
     * @param endDate the end date
     * @param limit maximum number of results
     * @param offset number of results to skip
     * @return Flux of audit logs
     */
    Flux<AuditLog> findByModuleAndDateRange(String module, LocalDateTime startDate, LocalDateTime endDate, int limit, long offset);

    /**
     * Searches audit logs by description or entity reference.
     *
     * @param search the search term
     * @param limit maximum number of results
     * @param offset number of results to skip
     * @return Flux of matching audit logs
     */
    Flux<AuditLog> searchByDescriptionOrReference(String search, int limit, long offset);

    /**
     * Counts search results.
     *
     * @param search the search term
     * @return Mono containing the count
     */
    Mono<Long> countSearchResults(String search);

    /**
     * Saves an audit log.
     *
     * @param auditLog the audit log to save
     * @return Mono containing the saved audit log
     */
    Mono<AuditLog> save(AuditLog auditLog);

    /**
     * Deletes audit logs older than a specific date.
     *
     * @param beforeDate the cutoff date
     * @return Mono containing the number of deleted records
     */
    Mono<Long> deleteOlderThan(LocalDateTime beforeDate);
}
