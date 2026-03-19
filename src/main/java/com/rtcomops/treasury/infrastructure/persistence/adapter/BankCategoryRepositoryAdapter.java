package com.rtcomops.treasury.infrastructure.persistence.adapter;

import com.rtcomops.treasury.domain.model.BankCategory;
import com.rtcomops.treasury.domain.port.out.BankCategoryRepositoryPort;
import com.rtcomops.treasury.infrastructure.persistence.mapper.BankCategoryPersistenceMapper;
import com.rtcomops.treasury.infrastructure.persistence.repository.R2dbcBankCategoryRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Adapter implementing the BankCategoryRepositoryPort using Spring Data R2DBC.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Component
public class BankCategoryRepositoryAdapter implements BankCategoryRepositoryPort {

    private final R2dbcBankCategoryRepository repository;
    private final BankCategoryPersistenceMapper mapper;

    public BankCategoryRepositoryAdapter(R2dbcBankCategoryRepository repository,
                                         BankCategoryPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<BankCategory> findById(UUID id) {
        return repository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<BankCategory> findByCode(String code) {
        return repository.findByCode(code)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<BankCategory> findAll() {
        return repository.findAll()
            .map(mapper::toDomain);
    }

    @Override
    public Mono<BankCategory> save(BankCategory bankCategory) {
        return repository.existsById(bankCategory.getId())
            .flatMap(exists -> {
                if (exists) {
                    return repository.save(mapper.toEntityForUpdate(bankCategory));
                } else {
                    return repository.save(mapper.toEntity(bankCategory));
                }
            })
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> delete(BankCategory bankCategory) {
        return repository.deleteById(bankCategory.getId());
    }
}
