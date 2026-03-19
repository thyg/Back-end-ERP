package com.rtcomops.treasury.infrastructure.persistence.adapter;

import com.rtcomops.treasury.domain.model.BankStatement;
import com.rtcomops.treasury.domain.port.out.BankStatementRepositoryPort;
import com.rtcomops.treasury.infrastructure.persistence.mapper.BankStatementPersistenceMapper;
import com.rtcomops.treasury.infrastructure.persistence.repository.R2dbcBankStatementRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Adapter implementing the BankStatementRepositoryPort using Spring Data R2DBC.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Component
public class BankStatementRepositoryAdapter implements BankStatementRepositoryPort {

    private final R2dbcBankStatementRepository repository;
    private final BankStatementPersistenceMapper mapper;

    public BankStatementRepositoryAdapter(R2dbcBankStatementRepository repository,
                                          BankStatementPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<BankStatement> findById(UUID id) {
        return repository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<BankStatement> findAllOrderByDateDesc() {
        return repository.findAllOrderByDateDesc()
            .map(mapper::toDomain);
    }

    @Override
    public Flux<BankStatement> findByBankAccountId(UUID accountId) {
        return repository.findByBankAccountId(accountId)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<BankStatement> findByBankAccountIdAndStatus(UUID accountId, String status) {
        return repository.findByBankAccountIdAndStatus(accountId, status)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<BankStatement> findByStatus(String status) {
        return repository.findByStatus(status)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<BankStatement> findByAccountIdAndPeriodContaining(UUID accountId, LocalDate date) {
        return repository.findByAccountIdAndPeriodContaining(accountId, date)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<BankStatement> findByAccountIdAndDateRange(UUID accountId, LocalDate startDate, LocalDate endDate) {
        return repository.findByAccountIdAndDateRange(accountId, startDate, endDate)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByAccountIdAndPeriod(UUID accountId, LocalDate periodStart, LocalDate periodEnd) {
        return repository.existsByAccountIdAndPeriod(accountId, periodStart, periodEnd);
    }

    @Override
    public Mono<BankStatement> save(BankStatement statement) {
        return repository.existsById(statement.getId())
            .flatMap(exists -> {
                if (exists) {
                    return repository.save(mapper.toEntityForUpdate(statement));
                } else {
                    return repository.save(mapper.toEntity(statement));
                }
            })
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> delete(BankStatement statement) {
        return repository.deleteById(statement.getId());
    }
}
