package com.rtcomops.treasury.domain.port.out;

import com.rtcomops.treasury.domain.model.Bank;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Output port for Bank repository operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
public interface BankRepositoryPort {

    Flux<Bank> findAll();

    Flux<Bank> findAllOrderByName();

    Flux<Bank> findAllActiveOrderByName();

    Mono<Bank> findById(UUID id);

    Mono<Bank> findByCode(String code);

    Mono<Boolean> existsByCode(String code);

    Mono<Boolean> existsByCodeAndIdNot(String code, UUID id);

    Mono<Bank> save(Bank bank);

    Mono<Void> deleteById(UUID id);
}
