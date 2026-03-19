package com.rtcomops.treasury.infrastructure.persistence.adapter;

import com.rtcomops.treasury.domain.model.AuditLog;
import com.rtcomops.treasury.domain.port.out.AuditLogRepositoryPort;
import com.rtcomops.treasury.infrastructure.persistence.mapper.AuditLogPersistenceMapper;
import com.rtcomops.treasury.infrastructure.persistence.repository.R2dbcAuditLogRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Adapter implementing the AuditLogRepositoryPort using Spring Data R2DBC.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-13
 */
@Component
public class AuditLogRepositoryAdapter implements AuditLogRepositoryPort {

    private final R2dbcAuditLogRepository repository;
    private final AuditLogPersistenceMapper mapper;

    public AuditLogRepositoryAdapter(R2dbcAuditLogRepository repository,
                                     AuditLogPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<AuditLog> findById(UUID id) {
        return repository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<AuditLog> findAllPaginated(int limit, long offset) {
        return repository.findAllPaginated(limit, offset)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countAll() {
        return repository.countAll();
    }

    @Override
    public Flux<AuditLog> findByModule(String module, int limit, long offset) {
        return repository.findByModule(module, limit, offset)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countByModule(String module) {
        return repository.countByModule(module);
    }

    @Override
    public Flux<AuditLog> findByAction(String action, int limit, long offset) {
        return repository.findByAction(action, limit, offset)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countByAction(String action) {
        return repository.countByAction(action);
    }

    @Override
    public Flux<AuditLog> findByEntityId(UUID entityId) {
        return repository.findByEntityId(entityId)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<AuditLog> findByUserId(UUID userId, int limit, long offset) {
        return repository.findByUserId(userId, limit, offset)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<AuditLog> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, int limit, long offset) {
        return repository.findByDateRange(startDate, endDate, limit, offset)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return repository.countByDateRange(startDate, endDate);
    }

    @Override
    public Flux<AuditLog> findToday() {
        return repository.findToday()
            .map(mapper::toDomain);
    }

    @Override
    public Flux<AuditLog> findByModuleAndAction(String module, String action, int limit, long offset) {
        return repository.findByModuleAndAction(module, action, limit, offset)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<AuditLog> findByModuleAndDateRange(String module, LocalDateTime startDate, LocalDateTime endDate, int limit, long offset) {
        return repository.findByModuleAndDateRange(module, startDate, endDate, limit, offset)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<AuditLog> searchByDescriptionOrReference(String search, int limit, long offset) {
        return repository.searchByDescriptionOrReference(search, limit, offset)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countSearchResults(String search) {
        return repository.countSearchResults(search);
    }

    @Override
    public Mono<Long> deleteOlderThan(LocalDateTime beforeDate) {
        return repository.deleteOlderThan(beforeDate);
    }

    @Override
    public Mono<AuditLog> save(AuditLog auditLog) {
        return repository.save(mapper.toEntity(auditLog))
            .map(mapper::toDomain);
    }
}
