package com.rtcomops.treasury.infrastructure.persistence.adapter;

import com.rtcomops.treasury.domain.model.Bank;
import com.rtcomops.treasury.domain.port.out.BankRepositoryPort;
import com.rtcomops.treasury.infrastructure.persistence.mapper.BankPersistenceMapper;
import com.rtcomops.treasury.infrastructure.persistence.repository.R2dbcBankRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Adapter implementing the BankRepositoryPort using Spring Data R2DBC.
 *
 * <p>This adapter bridges the domain layer with the persistence infrastructure.
 * It converts between domain models and persistence entities using the mapper.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@Component
public class BankRepositoryAdapter implements BankRepositoryPort {

    private final R2dbcBankRepository r2dbcBankRepository;
    private final BankPersistenceMapper mapper;

    /**
     * Constructs the adapter with required dependencies.
     *
     * @param r2dbcBankRepository the Spring Data R2DBC repository
     * @param mapper the persistence mapper
     */
    public BankRepositoryAdapter(R2dbcBankRepository r2dbcBankRepository,
                                  BankPersistenceMapper mapper) {
        this.r2dbcBankRepository = r2dbcBankRepository;
        this.mapper = mapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Bank> findById(UUID id) {
        return r2dbcBankRepository.findById(id)
            .map(mapper::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Bank> findByCode(String code) {
        return r2dbcBankRepository.findByCode(code)
            .map(mapper::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Boolean> existsByCode(String code) {
        return r2dbcBankRepository.existsByCode(code);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<Bank> findAllActiveOrderByName() {
        return r2dbcBankRepository.findAllActiveOrderByName()
            .map(mapper::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<Bank> findAllOrderByName() {
        return r2dbcBankRepository.findAllOrderByName()
            .map(mapper::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Boolean> existsByCodeAndIdNot(String code, UUID id) {
        return r2dbcBankRepository.existsByCodeAndIdNot(code, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Bank> save(Bank bank) {
        // Determine if this is an insert or update based on whether
        // the bank already exists in the database
        return r2dbcBankRepository.existsById(bank.getId())
            .flatMap(exists -> {
                if (exists) {
                    // Update: mark entity as not new
                    return r2dbcBankRepository.save(mapper.toEntityForUpdate(bank));
                } else {
                    // Insert: mark entity as new
                    return r2dbcBankRepository.save(mapper.toEntity(bank));
                }
            })
            .map(mapper::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<Bank> findAll() {
        return r2dbcBankRepository.findAll()
            .map(mapper::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> deleteById(UUID id) {
        return r2dbcBankRepository.deleteById(id);
    }
}
