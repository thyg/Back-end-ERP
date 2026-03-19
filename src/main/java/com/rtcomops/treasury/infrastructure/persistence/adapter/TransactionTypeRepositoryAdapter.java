package com.rtcomops.treasury.infrastructure.persistence.adapter;

import com.rtcomops.treasury.domain.model.TransactionType;
import com.rtcomops.treasury.domain.port.out.TransactionTypeRepositoryPort;
import com.rtcomops.treasury.infrastructure.persistence.mapper.TransactionTypePersistenceMapper;
import com.rtcomops.treasury.infrastructure.persistence.repository.R2dbcTransactionTypeRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Adapter implementing the TransactionTypeRepositoryPort using Spring Data R2DBC.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@Component
public class TransactionTypeRepositoryAdapter implements TransactionTypeRepositoryPort {

    private final R2dbcTransactionTypeRepository repository;
    private final TransactionTypePersistenceMapper mapper;

    public TransactionTypeRepositoryAdapter(R2dbcTransactionTypeRepository repository,
                                            TransactionTypePersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<TransactionType> findById(UUID id) {
        return repository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<TransactionType> findByCode(String code) {
        return repository.findByCode(code)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByCode(String code) {
        return repository.existsByCode(code);
    }

    @Override
    public Flux<TransactionType> findAllActiveOrderByCode() {
        return repository.findAllActiveOrderByCode()
            .map(mapper::toDomain);
    }

    @Override
    public Flux<TransactionType> findAllOrderByCode() {
        return repository.findAllOrderByCode()
            .map(mapper::toDomain);
    }

    @Override
    public Flux<TransactionType> findByCategory(String category) {
        return repository.findByCategory(category)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByCodeAndIdNot(String code, UUID id) {
        return repository.existsByCodeAndIdNot(code, id);
    }

    @Override
    public Mono<TransactionType> save(TransactionType transactionType) {
        return repository.existsById(transactionType.getId())
            .flatMap(exists -> {
                if (exists) {
                    return repository.save(mapper.toEntityForUpdate(transactionType));
                } else {
                    return repository.save(mapper.toEntity(transactionType));
                }
            })
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> delete(TransactionType transactionType) {
        return repository.deleteById(transactionType.getId());
    }
}
