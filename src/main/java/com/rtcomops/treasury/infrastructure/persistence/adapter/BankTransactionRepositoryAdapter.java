package com.rtcomops.treasury.infrastructure.persistence.adapter;

import com.rtcomops.treasury.domain.model.BankTransaction;
import com.rtcomops.treasury.domain.port.out.BankTransactionRepositoryPort;
import com.rtcomops.treasury.infrastructure.persistence.mapper.BankTransactionPersistenceMapper;
import com.rtcomops.treasury.infrastructure.persistence.repository.R2dbcBankTransactionRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Adapter implementing the BankTransactionRepositoryPort using Spring Data R2DBC.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Component
public class BankTransactionRepositoryAdapter implements BankTransactionRepositoryPort {

    private final R2dbcBankTransactionRepository repository;
    private final BankTransactionPersistenceMapper mapper;

    public BankTransactionRepositoryAdapter(R2dbcBankTransactionRepository repository,
                                            BankTransactionPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Flux<BankTransaction> findAllOrderByDateDesc() {
        return repository.findAllOrderByDateDesc()
            .map(mapper::toDomain);
    }

    @Override
    public Mono<BankTransaction> findById(UUID id) {
        return repository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<BankTransaction> findByBankAccountId(UUID accountId) {
        return repository.findByBankAccountId(accountId)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<BankTransaction> findByBankAccountIdAndStatus(UUID accountId, String status) {
        return repository.findByBankAccountIdAndStatus(accountId, status)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<BankTransaction> findByBankAccountIdAndDateRange(UUID accountId, LocalDate startDate, LocalDate endDate) {
        return repository.findByBankAccountIdAndDateRange(accountId, startDate, endDate)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<BankTransaction> findUnreconciledByAccountId(UUID accountId) {
        return repository.findUnreconciledByAccountId(accountId)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<BankTransaction> findByStatus(String status) {
        return repository.findByStatus(status)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByReference(String reference) {
        return repository.existsByReference(reference);
    }

    @Override
    public Mono<BankTransaction> save(BankTransaction transaction) {
        return repository.existsById(transaction.getId())
            .flatMap(exists -> {
                if (exists) {
                    return repository.save(mapper.toEntityForUpdate(transaction));
                } else {
                    return repository.save(mapper.toEntity(transaction));
                }
            })
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> delete(BankTransaction transaction) {
        return repository.deleteById(transaction.getId());
    }
}
