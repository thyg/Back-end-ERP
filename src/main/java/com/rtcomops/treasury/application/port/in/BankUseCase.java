package com.rtcomops.treasury.application.port.in;

import com.rtcomops.treasury.domain.model.Bank;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Input port for Bank use cases.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
public interface BankUseCase {

    Flux<Bank> findAll(boolean activeOnly);

    Mono<Bank> findById(UUID id);

    Mono<Bank> findByCode(String code);

    Mono<Bank> create(Bank bank);

    Mono<Bank> update(UUID id, Bank bank);

    Mono<Void> delete(UUID id);
}
