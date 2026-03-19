package com.rtcomops.treasury.infrastructure.persistence.repository;

import com.rtcomops.treasury.infrastructure.persistence.entity.BankStatementEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * R2DBC repository for BankStatementEntity persistence operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Repository
public interface R2dbcBankStatementRepository extends R2dbcRepository<BankStatementEntity, UUID> {

    @Query("SELECT * FROM treasury.bank_statements ORDER BY statement_date DESC, created_at DESC")
    Flux<BankStatementEntity> findAllOrderByDateDesc();

    @Query("SELECT * FROM treasury.bank_statements WHERE bank_account_id = :accountId ORDER BY statement_date DESC")
    Flux<BankStatementEntity> findByBankAccountId(UUID accountId);

    @Query("SELECT * FROM treasury.bank_statements WHERE bank_account_id = :accountId AND status = :status ORDER BY statement_date DESC")
    Flux<BankStatementEntity> findByBankAccountIdAndStatus(UUID accountId, String status);

    @Query("SELECT * FROM treasury.bank_statements WHERE status = :status ORDER BY statement_date DESC")
    Flux<BankStatementEntity> findByStatus(String status);

    @Query("SELECT * FROM treasury.bank_statements WHERE bank_account_id = :accountId AND period_start <= :date AND period_end >= :date")
    Mono<BankStatementEntity> findByAccountIdAndPeriodContaining(UUID accountId, LocalDate date);

    @Query("SELECT * FROM treasury.bank_statements WHERE bank_account_id = :accountId AND statement_date BETWEEN :startDate AND :endDate ORDER BY statement_date DESC")
    Flux<BankStatementEntity> findByAccountIdAndDateRange(UUID accountId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(*) > 0 FROM treasury.bank_statements WHERE bank_account_id = :accountId AND period_start = :periodStart AND period_end = :periodEnd")
    Mono<Boolean> existsByAccountIdAndPeriod(UUID accountId, LocalDate periodStart, LocalDate periodEnd);
}
