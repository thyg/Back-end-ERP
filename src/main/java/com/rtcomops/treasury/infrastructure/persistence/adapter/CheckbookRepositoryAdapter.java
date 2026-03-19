package com.rtcomops.treasury.infrastructure.persistence.adapter;

import com.rtcomops.treasury.domain.model.Checkbook;
import com.rtcomops.treasury.domain.port.out.CheckbookRepositoryPort;
import com.rtcomops.treasury.infrastructure.persistence.mapper.CheckbookPersistenceMapper;
import com.rtcomops.treasury.infrastructure.persistence.repository.R2dbcCheckbookRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Adapter implementing the CheckbookRepositoryPort using Spring Data R2DBC.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
@Component
public class CheckbookRepositoryAdapter implements CheckbookRepositoryPort {

    private final R2dbcCheckbookRepository repository;
    private final CheckbookPersistenceMapper mapper;

    public CheckbookRepositoryAdapter(R2dbcCheckbookRepository repository,
                                      CheckbookPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Checkbook> findById(UUID id) {
        return repository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<Checkbook> findAllOrderByCreatedAtDesc() {
        return repository.findAllOrderByCreatedAtDesc()
            .map(mapper::toDomain);
    }

    @Override
    public Flux<Checkbook> findByBankAccountId(UUID accountId) {
        return repository.findByBankAccountId(accountId)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Checkbook> findActiveByBankAccountId(UUID accountId) {
        return repository.findActiveByBankAccountId(accountId)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<Checkbook> findByStatus(String status) {
        return repository.findByStatus(status)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Checkbook> findByIsSystemTrue() {
        return repository.findByIsSystemTrue()
            .map(mapper::toDomain);
    }

    @Override
    public Flux<Checkbook> findByType(String type) {
        return repository.findByType(type)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<Checkbook> findAllRealCheckbooks() {
        return repository.findAllRealCheckbooks()
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsOverlappingRange(UUID accountId, Integer startNumber, Integer endNumber) {
        return repository.existsOverlappingRange(accountId, startNumber, endNumber);
    }

    @Override
    public Mono<Long> incrementCurrentNumber(UUID id) {
        return repository.incrementCurrentNumber(id);
    }

    @Override
    public Mono<Long> incrementNextSequence(UUID id) {
        return repository.incrementNextSequence(id);
    }

    @Override
    public Mono<Long> getNextSequence() {
        return repository.getNextSequence();
    }

    @Override
    public Mono<Checkbook> save(Checkbook checkbook) {
        return repository.existsById(checkbook.getId())
            .flatMap(exists -> {
                if (exists) {
                    return repository.save(mapper.toEntityForUpdate(checkbook));
                } else {
                    return repository.save(mapper.toEntity(checkbook));
                }
            })
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> delete(Checkbook checkbook) {
        return repository.deleteById(checkbook.getId());
    }
}
