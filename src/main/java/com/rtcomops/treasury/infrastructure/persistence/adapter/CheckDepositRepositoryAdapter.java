package com.rtcomops.treasury.infrastructure.persistence.adapter;

import com.rtcomops.treasury.domain.model.CheckDeposit;
import com.rtcomops.treasury.domain.port.out.CheckDepositRepositoryPort;
import com.rtcomops.treasury.infrastructure.persistence.mapper.CheckDepositPersistenceMapper;
import com.rtcomops.treasury.infrastructure.persistence.repository.R2dbcCheckDepositRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Adapter implementing the CheckDepositRepositoryPort using Spring Data R2DBC.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2026-02-16
 */
@Component
public class CheckDepositRepositoryAdapter implements CheckDepositRepositoryPort {

    private final R2dbcCheckDepositRepository repository;
    private final CheckDepositPersistenceMapper mapper;

    public CheckDepositRepositoryAdapter(R2dbcCheckDepositRepository repository,
                                         CheckDepositPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<CheckDeposit> findById(UUID id) {
        return repository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<CheckDeposit> findAllOrderByDateDesc() {
        return repository.findAllOrderByDateDesc()
            .map(mapper::toDomain);
    }

    @Override
    public Flux<CheckDeposit> findByBankAccountId(UUID bankAccountId) {
        return repository.findByBankAccountId(bankAccountId)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<CheckDeposit> findByStatus(String status) {
        return repository.findByStatus(status)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<CheckDeposit> findByReference(String reference) {
        return repository.findByReference(reference)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<CheckDeposit> findUnreconciled() {
        return repository.findUnreconciled()
            .map(mapper::toDomain);
    }

    @Override
    public Flux<CheckDeposit> findUnreconciledByAccountId(UUID bankAccountId) {
        return repository.findUnreconciledByAccountId(bankAccountId)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Integer> countByStatus(String status) {
        return repository.countByStatus(status);
    }

    @Override
    public Mono<BigDecimal> sumAmountByStatus(String status) {
        return repository.sumAmountByStatus(status);
    }

    @Override
    public Mono<CheckDeposit> save(CheckDeposit deposit) {
        return repository.existsById(deposit.getId())
            .flatMap(exists -> {
                if (exists) {
                    return repository.save(mapper.toEntityForUpdate(deposit));
                } else {
                    return repository.save(mapper.toEntity(deposit));
                }
            })
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> delete(CheckDeposit deposit) {
        return repository.deleteById(deposit.getId());
    }
}
