package com.rtcomops.treasury.infrastructure.persistence.adapter;

import com.rtcomops.treasury.domain.model.BankAccount;
import com.rtcomops.treasury.domain.port.out.BankAccountRepositoryPort;
import com.rtcomops.treasury.infrastructure.persistence.mapper.BankAccountPersistenceMapper;
import com.rtcomops.treasury.infrastructure.persistence.repository.R2dbcBankAccountRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Adapter implementing the BankAccountRepositoryPort using Spring Data R2DBC.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Component
public class BankAccountRepositoryAdapter implements BankAccountRepositoryPort {

    private final R2dbcBankAccountRepository repository;
    private final BankAccountPersistenceMapper mapper;

    public BankAccountRepositoryAdapter(R2dbcBankAccountRepository repository,
                                        BankAccountPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<BankAccount> findById(UUID id) {
        return repository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<BankAccount> findAllActiveOrderByName() {
        return repository.findAllActiveOrderByName()
            .map(mapper::toDomain);
    }

    @Override
    public Flux<BankAccount> findAllOrderByName() {
        return repository.findAllOrderByName()
            .map(mapper::toDomain);
    }

    @Override
    public Flux<BankAccount> findByBankId(UUID bankId) {
        return repository.findByBankId(bankId)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<BankAccount> save(BankAccount bankAccount) {
        return repository.existsById(bankAccount.getId())
            .flatMap(exists -> {
                if (exists) {
                    return repository.save(mapper.toEntityForUpdate(bankAccount));
                } else {
                    return repository.save(mapper.toEntity(bankAccount));
                }
            })
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> delete(BankAccount bankAccount) {
        return repository.deleteById(bankAccount.getId());
    }
}
