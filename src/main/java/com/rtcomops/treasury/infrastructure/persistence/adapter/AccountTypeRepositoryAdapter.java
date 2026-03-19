package com.rtcomops.treasury.infrastructure.persistence.adapter;

import com.rtcomops.treasury.domain.model.AccountType;
import com.rtcomops.treasury.domain.port.out.AccountTypeRepositoryPort;
import com.rtcomops.treasury.infrastructure.persistence.mapper.AccountTypePersistenceMapper;
import com.rtcomops.treasury.infrastructure.persistence.repository.R2dbcAccountTypeRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Adapter implementing the AccountTypeRepositoryPort using Spring Data R2DBC.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Component
public class AccountTypeRepositoryAdapter implements AccountTypeRepositoryPort {

    private final R2dbcAccountTypeRepository repository;
    private final AccountTypePersistenceMapper mapper;

    public AccountTypeRepositoryAdapter(R2dbcAccountTypeRepository repository,
                                        AccountTypePersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<AccountType> findById(UUID id) {
        return repository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<AccountType> findByCode(String code) {
        return repository.findByCode(code)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByCode(String code) {
        return repository.existsByCode(code);
    }

    @Override
    public Flux<AccountType> findAllActiveOrderByOrdre() {
        return repository.findAllActiveOrderByOrdre()
            .map(mapper::toDomain);
    }

    @Override
    public Flux<AccountType> findAllOrderByOrdre() {
        return repository.findAllOrderByOrdre()
            .map(mapper::toDomain);
    }

    @Override
    public Flux<AccountType> findByPeutEmettreChequesTrue() {
        return repository.findByPeutEmettreChequesTrue()
            .map(mapper::toDomain);
    }

    @Override
    public Flux<AccountType> findByPeutRecevoirChequesTrue() {
        return repository.findByPeutRecevoirChequesTrue()
            .map(mapper::toDomain);
    }

    @Override
    public Flux<AccountType> findByPeutTransactionsEspecesTrue() {
        return repository.findByPeutTransactionsEspecesTrue()
            .map(mapper::toDomain);
    }

    @Override
    public Flux<AccountType> findByDecouvertAutoriseTrue() {
        return repository.findByDecouvertAutoriseTrue()
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByCodeAndIdNot(String code, UUID id) {
        return repository.existsByCodeAndIdNot(code, id);
    }

    @Override
    public Mono<AccountType> save(AccountType accountType) {
        return repository.existsById(accountType.getId())
            .flatMap(exists -> {
                if (exists) {
                    return repository.save(mapper.toEntityForUpdate(accountType));
                } else {
                    return repository.save(mapper.toEntity(accountType));
                }
            })
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> delete(AccountType accountType) {
        return repository.deleteById(accountType.getId());
    }
}
