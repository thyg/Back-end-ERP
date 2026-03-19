package com.rtcomops.treasury.infrastructure.persistence.adapter;

import com.rtcomops.treasury.domain.model.AccountConnectorType;
import com.rtcomops.treasury.domain.port.out.AccountConnectorTypeRepositoryPort;
import com.rtcomops.treasury.infrastructure.persistence.mapper.AccountConnectorTypePersistenceMapper;
import com.rtcomops.treasury.infrastructure.persistence.repository.R2dbcAccountConnectorTypeRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Adapter implementing the AccountConnectorTypeRepositoryPort using Spring Data R2DBC.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Component
public class AccountConnectorTypeRepositoryAdapter implements AccountConnectorTypeRepositoryPort {

    private final R2dbcAccountConnectorTypeRepository repository;
    private final AccountConnectorTypePersistenceMapper mapper;

    public AccountConnectorTypeRepositoryAdapter(R2dbcAccountConnectorTypeRepository repository,
                                                 AccountConnectorTypePersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<AccountConnectorType> findById(UUID id) {
        return repository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<AccountConnectorType> findByBankCategoryId(UUID bankCategoryId) {
        return repository.findByBankCategoryId(bankCategoryId)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<AccountConnectorType> findAll() {
        return repository.findAll()
            .map(mapper::toDomain);
    }

    @Override
    public Mono<AccountConnectorType> save(AccountConnectorType connectorType) {
        return repository.existsById(connectorType.getId())
            .flatMap(exists -> {
                if (exists) {
                    return repository.save(mapper.toEntityForUpdate(connectorType));
                } else {
                    return repository.save(mapper.toEntity(connectorType));
                }
            })
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> delete(AccountConnectorType connectorType) {
        return repository.deleteById(connectorType.getId());
    }
}
