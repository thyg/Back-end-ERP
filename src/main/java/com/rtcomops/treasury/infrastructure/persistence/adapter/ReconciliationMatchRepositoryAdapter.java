package com.rtcomops.treasury.infrastructure.persistence.adapter;

import com.rtcomops.treasury.domain.model.ReconciliationMatch;
import com.rtcomops.treasury.domain.port.out.ReconciliationMatchRepositoryPort;
import com.rtcomops.treasury.infrastructure.persistence.mapper.ReconciliationMatchPersistenceMapper;
import com.rtcomops.treasury.infrastructure.persistence.repository.R2dbcReconciliationMatchRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Adapter implementing the ReconciliationMatchRepositoryPort using Spring Data R2DBC.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Component
public class ReconciliationMatchRepositoryAdapter implements ReconciliationMatchRepositoryPort {

    private final R2dbcReconciliationMatchRepository repository;
    private final ReconciliationMatchPersistenceMapper mapper;

    public ReconciliationMatchRepositoryAdapter(R2dbcReconciliationMatchRepository repository,
                                                ReconciliationMatchPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<ReconciliationMatch> findById(UUID id) {
        return repository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<ReconciliationMatch> findByStatementLineId(UUID statementLineId) {
        return repository.findByStatementLineId(statementLineId)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<ReconciliationMatch> findByBankStatementId(UUID bankStatementId) {
        // This method finds all matches for lines belonging to a specific statement
        // Since we don't have a direct query for this, we'll return empty for now
        // This should be implemented with a proper join query if needed
        return Flux.empty();
    }

    @Override
    public Mono<Boolean> existsByStatementLineId(UUID statementLineId) {
        return repository.existsByStatementLineId(statementLineId);
    }

    @Override
    public Mono<ReconciliationMatch> save(ReconciliationMatch match) {
        return repository.existsById(match.getId())
            .flatMap(exists -> {
                if (exists) {
                    return repository.save(mapper.toEntityForUpdate(match));
                } else {
                    return repository.save(mapper.toEntity(match));
                }
            })
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Void> deleteByStatementLineId(UUID statementLineId) {
        return repository.deleteByStatementLineId(statementLineId);
    }

    @Override
    public Mono<Long> countByBankStatementId(UUID bankStatementId) {
        // This should be implemented with a proper join query if needed
        return Mono.just(0L);
    }

    @Override
    public Flux<ReconciliationMatch> findAll() {
        return repository.findAll()
            .map(mapper::toDomain);
    }
}
