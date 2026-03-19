package com.rtcomops.treasury.infrastructure.persistence.repository;

import com.rtcomops.treasury.infrastructure.persistence.entity.CheckDepositEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * R2DBC repository for CheckDepositEntity persistence operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2026-02-16
 */
@Repository
public interface R2dbcCheckDepositRepository extends R2dbcRepository<CheckDepositEntity, UUID> {

    @Query("SELECT * FROM treasury.check_deposits ORDER BY deposit_date DESC, created_at DESC")
    Flux<CheckDepositEntity> findAllOrderByDateDesc();

    @Query("SELECT * FROM treasury.check_deposits WHERE bank_account_id = :bankAccountId ORDER BY deposit_date DESC")
    Flux<CheckDepositEntity> findByBankAccountId(UUID bankAccountId);

    @Query("SELECT * FROM treasury.check_deposits WHERE status = :status ORDER BY deposit_date DESC")
    Flux<CheckDepositEntity> findByStatus(String status);

    @Query("SELECT * FROM treasury.check_deposits WHERE reference = :reference")
    Mono<CheckDepositEntity> findByReference(String reference);

    @Query("SELECT * FROM treasury.check_deposits WHERE status = 'DEPOSITED' ORDER BY deposit_date ASC")
    Flux<CheckDepositEntity> findUnreconciled();

    @Query("SELECT * FROM treasury.check_deposits WHERE bank_account_id = :bankAccountId AND status = 'DEPOSITED' ORDER BY deposit_date ASC")
    Flux<CheckDepositEntity> findUnreconciledByAccountId(UUID bankAccountId);

    @Query("SELECT COUNT(*) FROM treasury.check_deposits WHERE status = :status")
    Mono<Integer> countByStatus(String status);

    @Query("SELECT COALESCE(SUM(total_amount), 0) FROM treasury.check_deposits WHERE status = :status")
    Mono<BigDecimal> sumAmountByStatus(String status);
}
