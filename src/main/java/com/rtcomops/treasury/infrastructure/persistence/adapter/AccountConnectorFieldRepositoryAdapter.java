package com.rtcomops.treasury.infrastructure.persistence.adapter;

import com.rtcomops.treasury.domain.model.AccountConnectorField;
import com.rtcomops.treasury.domain.port.out.AccountConnectorFieldRepositoryPort;
import com.rtcomops.treasury.infrastructure.persistence.mapper.AccountConnectorFieldPersistenceMapper;
import com.rtcomops.treasury.infrastructure.persistence.repository.R2dbcAccountConnectorFieldRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Adapter implementing the AccountConnectorFieldRepositoryPort using Spring Data R2DBC.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Component
public class AccountConnectorFieldRepositoryAdapter implements AccountConnectorFieldRepositoryPort {

    private final R2dbcAccountConnectorFieldRepository repository;
    private final AccountConnectorFieldPersistenceMapper mapper;

    public AccountConnectorFieldRepositoryAdapter(R2dbcAccountConnectorFieldRepository repository,
                                                  AccountConnectorFieldPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<AccountConnectorField> findById(UUID id) {
        return repository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<AccountConnectorField> findByConnectorTypeId(UUID connectorTypeId) {
        return repository.findByConnectorTypeId(connectorTypeId)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<AccountConnectorField> findAll() {
        return repository.findAll()
            .map(mapper::toDomain);
    }

    @Override
    public Mono<AccountConnectorField> save(AccountConnectorField field) {
        return repository.existsById(field.getId())
            .flatMap(exists -> {
                if (exists) {
                    return repository.save(mapper.toEntityForUpdate(field));
                } else {
                    return repository.save(mapper.toEntity(field));
                }
            })
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> delete(AccountConnectorField field) {
        return repository.deleteById(field.getId());
    }
}
