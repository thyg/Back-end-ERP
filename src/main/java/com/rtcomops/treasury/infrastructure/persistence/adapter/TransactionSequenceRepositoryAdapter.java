package com.rtcomops.treasury.infrastructure.persistence.adapter;

import com.rtcomops.treasury.domain.model.TransactionSequence;
import com.rtcomops.treasury.domain.port.out.TransactionSequenceRepositoryPort;
import com.rtcomops.treasury.infrastructure.persistence.mapper.TransactionSequencePersistenceMapper;
import com.rtcomops.treasury.infrastructure.persistence.repository.R2dbcTransactionSequenceRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Adapter implementing the TransactionSequenceRepositoryPort using Spring Data R2DBC.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
@Component
public class TransactionSequenceRepositoryAdapter implements TransactionSequenceRepositoryPort {

    private final R2dbcTransactionSequenceRepository repository;
    private final TransactionSequencePersistenceMapper mapper;

    public TransactionSequenceRepositoryAdapter(R2dbcTransactionSequenceRepository repository,
                                                TransactionSequencePersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<TransactionSequence> findByTypeCodeAndYearMonth(String typeCode, String yearMonth) {
        return repository.findByTypeCodeAndYearMonth(typeCode, yearMonth)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> incrementSequence(String typeCode, String yearMonth) {
        return repository.incrementSequence(typeCode, yearMonth);
    }

    @Override
    public Mono<Integer> findLastSequence(String typeCode, String yearMonth) {
        return repository.findLastSequence(typeCode, yearMonth);
    }

    @Override
    public Mono<TransactionSequence> save(TransactionSequence sequence) {
        return repository.existsById(sequence.getId())
            .flatMap(exists -> {
                if (exists) {
                    return repository.save(mapper.toEntityForUpdate(sequence));
                } else {
                    return repository.save(mapper.toEntity(sequence));
                }
            })
            .map(mapper::toDomain);
    }

    @Override
    public Mono<TransactionSequence> findById(UUID id) {
        return repository.findById(id)
            .map(mapper::toDomain);
    }
}
