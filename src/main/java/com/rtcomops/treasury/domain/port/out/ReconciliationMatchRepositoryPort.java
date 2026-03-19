package com.rtcomops.treasury.domain.port.out;

import com.rtcomops.treasury.domain.model.ReconciliationMatch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Output port for ReconciliationMatch repository operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
public interface ReconciliationMatchRepositoryPort {

    Mono<ReconciliationMatch> findById(UUID id);

    Flux<ReconciliationMatch> findByStatementLineId(UUID statementLineId);

    Flux<ReconciliationMatch> findByBankStatementId(UUID bankStatementId);

    Mono<Boolean> existsByStatementLineId(UUID statementLineId);

    Mono<ReconciliationMatch> save(ReconciliationMatch match);

    Mono<Void> deleteById(UUID id);

    Mono<Void> deleteByStatementLineId(UUID statementLineId);

    Mono<Long> countByBankStatementId(UUID bankStatementId);

    Flux<ReconciliationMatch> findAll();
}
