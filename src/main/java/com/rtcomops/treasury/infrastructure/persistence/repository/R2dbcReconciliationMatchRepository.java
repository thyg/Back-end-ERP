package com.rtcomops.treasury.infrastructure.persistence.repository;

import com.rtcomops.treasury.infrastructure.persistence.entity.ReconciliationMatchEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * R2DBC repository for ReconciliationMatchEntity persistence operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Repository
public interface R2dbcReconciliationMatchRepository extends R2dbcRepository<ReconciliationMatchEntity, UUID> {

    @Query("SELECT * FROM treasury.reconciliation_matches WHERE statement_line_id = :lineId")
    Flux<ReconciliationMatchEntity> findByStatementLineId(UUID lineId);

    @Query("SELECT * FROM treasury.reconciliation_matches WHERE bank_transaction_id = :transactionId")
    Flux<ReconciliationMatchEntity> findByBankTransactionId(UUID transactionId);

    @Query("SELECT * FROM treasury.reconciliation_matches WHERE check_id = :checkId")
    Flux<ReconciliationMatchEntity> findByCheckId(UUID checkId);

    @Query("SELECT * FROM treasury.reconciliation_matches WHERE match_method = :method ORDER BY created_at DESC")
    Flux<ReconciliationMatchEntity> findByMatchMethod(String method);

    @Query("DELETE FROM treasury.reconciliation_matches WHERE statement_line_id = :lineId")
    Mono<Void> deleteByStatementLineId(UUID lineId);

    @Query("SELECT COUNT(*) > 0 FROM treasury.reconciliation_matches WHERE statement_line_id = :lineId")
    Mono<Boolean> existsByStatementLineId(UUID lineId);

    @Query("SELECT COUNT(*) > 0 FROM treasury.reconciliation_matches WHERE bank_transaction_id = :transactionId")
    Mono<Boolean> existsByBankTransactionId(UUID transactionId);

    @Query("SELECT COUNT(*) > 0 FROM treasury.reconciliation_matches WHERE check_id = :checkId")
    Mono<Boolean> existsByCheckId(UUID checkId);
}
