package com.rtcomops.treasury.service;

import com.rtcomops.treasury.dto.request.AuditLogFilterRequest;
import com.rtcomops.treasury.dto.response.AuditLogResponse;
import com.rtcomops.treasury.entity.AuditLog;
import com.rtcomops.treasury.enums.AuditAction;
import com.rtcomops.treasury.enums.AuditModule;
import com.rtcomops.treasury.mapper.AuditLogMapper;
import com.rtcomops.treasury.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Service layer for AuditLog operations.
 *
 * <p>Provides methods for:</p>
 * <ul>
 *   <li>Logging audit entries from other services</li>
 *   <li>Querying audit logs with various filters</li>
 *   <li>Exporting audit data</li>
 * </ul>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-13
 */
@Service
@Transactional
public class AuditLogService {

    private static final Logger LOG = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    public AuditLogService(AuditLogRepository auditLogRepository, AuditLogMapper auditLogMapper) {
        this.auditLogRepository = auditLogRepository;
        this.auditLogMapper = auditLogMapper;
    }

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
    public Mono<Void> log(
            AuditModule module,
            AuditAction action,
            UUID entityId,
            String entityReference,
            Object oldValue,
            Object newValue,
            String description) {

        return log(module, action, entityId, entityReference, oldValue, newValue, 
                   description, null, "Système", null, null);
    }

    /**
     * Logs an audit entry with user context.
     */
    public Mono<Void> log(
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
            String userAgent) {

        LOG.debug("Logging audit: module={}, action={}, entityId={}, ref={}",
                module, action, entityId, entityReference);

        AuditLog entity = auditLogMapper.createEntity(
            module, action, entityId, entityReference,
            oldValue, newValue, description,
            userId, userName, ipAddress, userAgent
        );

        return auditLogRepository.save(entity)
            .doOnSuccess(saved -> LOG.info("Audit logged: {} {} on {} ({})",
                action.getLabel(), module.getLabel(), entityReference, entityId))
            .doOnError(error -> LOG.error("Failed to log audit: {}", error.getMessage()))
            .then();
    }

    /**
     * Simplified log method for CREATE operations.
     */
    public Mono<Void> logCreate(AuditModule module, UUID entityId, String reference, Object entity, String description) {
        return log(module, AuditAction.CREATE, entityId, reference, null, entity, description);
    }

    /**
     * Simplified log method for UPDATE operations.
     */
    public Mono<Void> logUpdate(AuditModule module, UUID entityId, String reference, Object oldEntity, Object newEntity, String description) {
        return log(module, AuditAction.UPDATE, entityId, reference, oldEntity, newEntity, description);
    }

    /**
     * Simplified log method for DELETE operations.
     */
    public Mono<Void> logDelete(AuditModule module, UUID entityId, String reference, Object entity, String description) {
        return log(module, AuditAction.DELETE, entityId, reference, entity, null, description);
    }

    // =========================================================================
    // QUERY METHODS
    // =========================================================================

    /**
     * Finds audit logs with filters.
     */
    @Transactional(readOnly = true)
    public Flux<AuditLogResponse> findWithFilters(AuditLogFilterRequest filter) {
        LOG.debug("Finding audit logs with filters: {}", filter);

        int limit = filter.getSize() != null ? filter.getSize() : 50;
        long offset = (filter.getPage() != null ? filter.getPage() : 0) * limit;

        // Determine which query to use based on filters
        Flux<AuditLog> results;

        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            results = auditLogRepository.searchByDescriptionOrReference(filter.getSearch(), limit, offset);
        } else if (filter.getEntityId() != null) {
            results = auditLogRepository.findByEntityId(filter.getEntityId());
        } else if (filter.getModule() != null && filter.getAction() != null) {
            results = auditLogRepository.findByModuleAndAction(filter.getModule(), filter.getAction(), limit, offset);
        } else if (filter.getModule() != null && filter.getStartDate() != null && filter.getEndDate() != null) {
            results = auditLogRepository.findByModuleAndDateRange(
                filter.getModule(),
                filter.getStartDate().atStartOfDay(),
                filter.getEndDate().atTime(LocalTime.MAX),
                limit, offset
            );
        } else if (filter.getModule() != null) {
            results = auditLogRepository.findByModule(filter.getModule(), limit, offset);
        } else if (filter.getAction() != null) {
            results = auditLogRepository.findByAction(filter.getAction(), limit, offset);
        } else if (filter.getUserId() != null) {
            results = auditLogRepository.findByUserId(filter.getUserId(), limit, offset);
        } else if (filter.getStartDate() != null && filter.getEndDate() != null) {
            results = auditLogRepository.findByDateRange(
                filter.getStartDate().atStartOfDay(),
                filter.getEndDate().atTime(LocalTime.MAX),
                limit, offset
            );
        } else {
            results = auditLogRepository.findAllPaginated(limit, offset);
        }

        return results.map(auditLogMapper::toResponse);
    }

    /**
     * Counts audit logs with filters.
     */
    @Transactional(readOnly = true)
    public Mono<Long> countWithFilters(AuditLogFilterRequest filter) {
        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            return auditLogRepository.countSearchResults(filter.getSearch());
        } else if (filter.getModule() != null) {
            return auditLogRepository.countByModule(filter.getModule());
        } else if (filter.getAction() != null) {
            return auditLogRepository.countByAction(filter.getAction());
        } else if (filter.getStartDate() != null && filter.getEndDate() != null) {
            return auditLogRepository.countByDateRange(
                filter.getStartDate().atStartOfDay(),
                filter.getEndDate().atTime(LocalTime.MAX)
            );
        }
        return auditLogRepository.countAll();
    }

    /**
     * Finds audit log by ID.
     */
    @Transactional(readOnly = true)
    public Mono<AuditLogResponse> findById(UUID id) {
        LOG.debug("Finding audit log by id={}", id);
        return auditLogRepository.findById(id)
            .map(auditLogMapper::toResponse);
    }

    /**
     * Finds all audit logs for a specific entity (history).
     */
    @Transactional(readOnly = true)
    public Flux<AuditLogResponse> findByEntityId(UUID entityId) {
        LOG.debug("Finding audit history for entityId={}", entityId);
        return auditLogRepository.findByEntityId(entityId)
            .map(auditLogMapper::toResponse);
    }

    /**
     * Finds today's audit logs.
     */
    @Transactional(readOnly = true)
    public Flux<AuditLogResponse> findToday() {
        LOG.debug("Finding today's audit logs");
        return auditLogRepository.findToday()
            .map(auditLogMapper::toResponse);
    }

    // =========================================================================
    // CLEANUP
    // =========================================================================

    /**
     * Deletes audit logs older than the specified number of days.
     * Should be called by a scheduled job.
     */
    public Mono<Long> cleanup(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        LOG.info("Cleaning up audit logs older than {}", cutoffDate);
        return auditLogRepository.deleteOlderThan(cutoffDate)
            .doOnSuccess(count -> LOG.info("Deleted {} old audit logs", count));
    }
}