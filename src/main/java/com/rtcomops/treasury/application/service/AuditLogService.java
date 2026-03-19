package com.rtcomops.treasury.application.service;

import com.rtcomops.treasury.application.dto.request.AuditLogFilterRequest;
import com.rtcomops.treasury.application.dto.response.AuditLogResponse;
import com.rtcomops.treasury.application.mapper.AuditLogMapper;
import com.rtcomops.treasury.domain.model.AuditLog;
import com.rtcomops.treasury.domain.model.enums.AuditAction;
import com.rtcomops.treasury.domain.model.enums.AuditModule;
import com.rtcomops.treasury.application.port.in.AuditLogUseCase;
import com.rtcomops.treasury.domain.port.out.AuditLogRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Domain service for AuditLog operations.
 *
 * <p>Implements the AuditLogUseCase port and provides business logic
 * for audit logging and querying audit history.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-13
 */
@Service
@Transactional
public class AuditLogService implements AuditLogUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepositoryPort auditLogRepositoryPort;
    private final AuditLogMapper auditLogMapper;

    public AuditLogService(
            AuditLogRepositoryPort auditLogRepositoryPort,
            AuditLogMapper auditLogMapper) {
        this.auditLogRepositoryPort = auditLogRepositoryPort;
        this.auditLogMapper = auditLogMapper;
    }

    // =========================================================================
    // LOGGING METHODS
    // =========================================================================

    @Override
    public Mono<Void> log(
            AuditModule module,
            AuditAction action,
            UUID entityId,
            String entityReference,
            Object oldValue,
            Object newValue,
            String description) {
        return log(module, action, entityId, entityReference, oldValue, newValue,
                   description, null, "Systeme", null, null);
    }

    @Override
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

        AuditLog domain = auditLogMapper.toDomain(
            module, action, entityId, entityReference,
            oldValue, newValue, description,
            userId, userName, ipAddress, userAgent
        );

        return auditLogRepositoryPort.save(domain)
            .doOnSuccess(saved -> LOG.info("Audit logged: {} {} on {} ({})",
                action.getLabel(), module.getLabel(), entityReference, entityId))
            .doOnError(error -> LOG.error("Failed to log audit: {}", error.getMessage()))
            .then();
    }

    @Override
    public Mono<Void> logCreate(AuditModule module, UUID entityId, String reference, Object entity, String description) {
        return log(module, AuditAction.CREATE, entityId, reference, null, entity, description);
    }

    @Override
    public Mono<Void> logUpdate(AuditModule module, UUID entityId, String reference, Object oldEntity, Object newEntity, String description) {
        return log(module, AuditAction.UPDATE, entityId, reference, oldEntity, newEntity, description);
    }

    @Override
    public Mono<Void> logDelete(AuditModule module, UUID entityId, String reference, Object entity, String description) {
        return log(module, AuditAction.DELETE, entityId, reference, entity, null, description);
    }

    // =========================================================================
    // QUERY METHODS
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public Flux<AuditLogResponse> findWithFilters(AuditLogFilterRequest filter) {
        LOG.debug("Finding audit logs with filters: {}", filter);

        int limit = filter.getSize() != null ? filter.getSize() : 50;
        long offset = (filter.getPage() != null ? filter.getPage() : 0) * limit;

        Flux<AuditLog> results;

        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            results = auditLogRepositoryPort.searchByDescriptionOrReference(filter.getSearch(), limit, offset);
        } else if (filter.getEntityId() != null) {
            results = auditLogRepositoryPort.findByEntityId(filter.getEntityId());
        } else if (filter.getModule() != null && filter.getAction() != null) {
            results = auditLogRepositoryPort.findByModuleAndAction(filter.getModule(), filter.getAction(), limit, offset);
        } else if (filter.getModule() != null && filter.getStartDate() != null && filter.getEndDate() != null) {
            results = auditLogRepositoryPort.findByModuleAndDateRange(
                filter.getModule(),
                filter.getStartDate().atStartOfDay(),
                filter.getEndDate().atTime(LocalTime.MAX),
                limit, offset
            );
        } else if (filter.getModule() != null) {
            results = auditLogRepositoryPort.findByModule(filter.getModule(), limit, offset);
        } else if (filter.getAction() != null) {
            results = auditLogRepositoryPort.findByAction(filter.getAction(), limit, offset);
        } else if (filter.getUserId() != null) {
            results = auditLogRepositoryPort.findByUserId(filter.getUserId(), limit, offset);
        } else if (filter.getStartDate() != null && filter.getEndDate() != null) {
            results = auditLogRepositoryPort.findByDateRange(
                filter.getStartDate().atStartOfDay(),
                filter.getEndDate().atTime(LocalTime.MAX),
                limit, offset
            );
        } else {
            results = auditLogRepositoryPort.findAllPaginated(limit, offset);
        }

        return results.map(auditLogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Long> countWithFilters(AuditLogFilterRequest filter) {
        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            return auditLogRepositoryPort.countSearchResults(filter.getSearch());
        } else if (filter.getModule() != null) {
            return auditLogRepositoryPort.countByModule(filter.getModule());
        } else if (filter.getAction() != null) {
            return auditLogRepositoryPort.countByAction(filter.getAction());
        } else if (filter.getStartDate() != null && filter.getEndDate() != null) {
            return auditLogRepositoryPort.countByDateRange(
                filter.getStartDate().atStartOfDay(),
                filter.getEndDate().atTime(LocalTime.MAX)
            );
        }
        return auditLogRepositoryPort.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<AuditLogResponse> findById(UUID id) {
        LOG.debug("Finding audit log by id={}", id);
        return auditLogRepositoryPort.findById(id)
            .map(auditLogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<AuditLogResponse> findByEntityId(UUID entityId) {
        LOG.debug("Finding audit history for entityId={}", entityId);
        return auditLogRepositoryPort.findByEntityId(entityId)
            .map(auditLogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<AuditLogResponse> findToday() {
        LOG.debug("Finding today's audit logs");
        return auditLogRepositoryPort.findToday()
            .map(auditLogMapper::toResponse);
    }

    // =========================================================================
    // CLEANUP
    // =========================================================================

    @Override
    public Mono<Long> cleanup(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        LOG.info("Cleaning up audit logs older than {}", cutoffDate);
        return auditLogRepositoryPort.deleteOlderThan(cutoffDate)
            .doOnSuccess(count -> LOG.info("Deleted {} old audit logs", count));
    }
}
