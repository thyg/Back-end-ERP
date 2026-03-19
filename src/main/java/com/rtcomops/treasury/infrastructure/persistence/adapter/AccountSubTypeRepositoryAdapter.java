package com.rtcomops.treasury.infrastructure.persistence.adapter;

import com.rtcomops.treasury.domain.model.AccountSubType;
import com.rtcomops.treasury.domain.port.out.AccountSubTypeRepositoryPort;
import com.rtcomops.treasury.infrastructure.persistence.mapper.AccountSubTypePersistenceMapper;
import com.rtcomops.treasury.infrastructure.persistence.repository.R2dbcAccountSubTypeRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Adapter implementing the AccountSubTypeRepositoryPort using Spring Data R2DBC.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Component
public class AccountSubTypeRepositoryAdapter implements AccountSubTypeRepositoryPort {

    private final R2dbcAccountSubTypeRepository repository;
    private final AccountSubTypePersistenceMapper mapper;

    public AccountSubTypeRepositoryAdapter(R2dbcAccountSubTypeRepository repository,
                                           AccountSubTypePersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<AccountSubType> findById(UUID id) {
        return repository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<AccountSubType> findByAccountTypeId(UUID accountTypeId) {
        return repository.findByAccountTypeId(accountTypeId)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<AccountSubType> findByAccountTypeIdAndIsActiveTrue(UUID accountTypeId) {
        return repository.findByAccountTypeIdAndIsActiveTrue(accountTypeId)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<AccountSubType> findByAccountTypeIdAndCode(UUID accountTypeId, String code) {
        return repository.findByAccountTypeIdAndCode(accountTypeId, code)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByAccountTypeIdAndCode(UUID accountTypeId, String code) {
        return repository.existsByAccountTypeIdAndCode(accountTypeId, code);
    }

    @Override
    public Mono<Boolean> existsByAccountTypeIdAndCodeAndIdNot(UUID accountTypeId, String code, UUID id) {
        return repository.existsByAccountTypeIdAndCodeAndIdNot(accountTypeId, code, id);
    }

    @Override
    public Flux<AccountSubType> findAllActiveOrderByOrdre() {
        return repository.findAllActiveOrderByOrdre()
            .map(mapper::toDomain);
    }

    @Override
    public Flux<AccountSubType> findAllOrderByOrdre() {
        return repository.findAllOrderByOrdre()
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countByAccountTypeId(UUID accountTypeId) {
        return repository.countByAccountTypeId(accountTypeId);
    }

    @Override
    public Mono<Void> deleteByAccountTypeId(UUID accountTypeId) {
        return repository.deleteByAccountTypeId(accountTypeId);
    }

    @Override
    public Mono<AccountSubType> save(AccountSubType accountSubType) {
        return repository.existsById(accountSubType.getId())
            .flatMap(exists -> {
                if (exists) {
                    return repository.save(mapper.toEntityForUpdate(accountSubType));
                } else {
                    return repository.save(mapper.toEntity(accountSubType));
                }
            })
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> delete(AccountSubType accountSubType) {
        return repository.deleteById(accountSubType.getId());
    }
}
