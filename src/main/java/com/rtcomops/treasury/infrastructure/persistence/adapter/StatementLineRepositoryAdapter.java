package com.rtcomops.treasury.infrastructure.persistence.adapter;

import com.rtcomops.treasury.domain.model.StatementLine;
import com.rtcomops.treasury.domain.port.out.StatementLineRepositoryPort;
import com.rtcomops.treasury.infrastructure.persistence.mapper.StatementLinePersistenceMapper;
import com.rtcomops.treasury.infrastructure.persistence.repository.R2dbcStatementLineRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.StreamSupport;

/**
 * Adapter implementing the StatementLineRepositoryPort using Spring Data R2DBC.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Component
public class StatementLineRepositoryAdapter implements StatementLineRepositoryPort {

    private final R2dbcStatementLineRepository repository;
    private final StatementLinePersistenceMapper mapper;

    public StatementLineRepositoryAdapter(R2dbcStatementLineRepository repository,
                                          StatementLinePersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<StatementLine> findById(UUID id) {
        return repository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<StatementLine> findByBankStatementIdOrderByLineNumber(UUID statementId) {
        return repository.findByBankStatementIdOrderByLineNumber(statementId)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<StatementLine> findByStatementIdAndStatus(UUID statementId, String status) {
        return repository.findByStatementIdAndStatus(statementId, status)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<StatementLine> findUnmatchedByStatementId(UUID statementId) {
        return repository.findUnmatchedByStatementId(statementId)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<StatementLine> findMatchedByStatementId(UUID statementId) {
        return repository.findMatchedByStatementId(statementId)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Integer> findMaxLineNumberByStatementId(UUID statementId) {
        return repository.findMaxLineNumberByStatementId(statementId);
    }

    @Override
    public Mono<Long> countByStatementId(UUID statementId) {
        return repository.countByStatementId(statementId);
    }

    @Override
    public Mono<Long> countMatchedByStatementId(UUID statementId) {
        return repository.countMatchedByStatementId(statementId);
    }

    @Override
    public Flux<StatementLine> findUnmatchedByAccountAndAmountAndDirection(UUID accountId, BigDecimal amount, String direction) {
        return repository.findUnmatchedByAccountAndAmountAndDirection(accountId, amount, direction)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<StatementLine> findUnmatchedByAccountAndDateRange(UUID accountId, LocalDate startDate, LocalDate endDate) {
        return repository.findUnmatchedByAccountAndDateRange(accountId, startDate, endDate)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<StatementLine> save(StatementLine line) {
        return repository.existsById(line.getId())
            .flatMap(exists -> {
                if (exists) {
                    return repository.save(mapper.toEntityForUpdate(line));
                } else {
                    return repository.save(mapper.toEntity(line));
                }
            })
            .map(mapper::toDomain);
    }

    @Override
    public Flux<StatementLine> saveAll(Iterable<StatementLine> lines) {
        var entities = StreamSupport.stream(lines.spliterator(), false)
            .map(mapper::toEntity)
            .toList();
        return repository.saveAll(entities)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> delete(StatementLine line) {
        return repository.deleteById(line.getId());
    }

    @Override
    public Mono<Void> deleteByStatementId(UUID statementId) {
        return repository.findByBankStatementIdOrderByLineNumber(statementId)
            .flatMap(entity -> repository.deleteById(entity.getId()))
            .then();
    }
}
