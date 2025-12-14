package com.rtcomops.treasury.repository;

import com.rtcomops.treasury.entity.BankStatement;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Reactive repository for BankStatement entity operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Repository
public interface BankStatementRepository extends R2dbcRepository<BankStatement, UUID> {

    @Query("SELECT * FROM treasury.bank_statements ORDER BY statement_date DESC, created_at DESC")
    Flux<BankStatement> findAllOrderByDateDesc();

    @Query("SELECT * FROM treasury.bank_statements WHERE bank_account_id = :accountId ORDER BY statement_date DESC")
    Flux<BankStatement> findByBankAccountId(UUID accountId);

    @Query("SELECT * FROM treasury.bank_statements WHERE bank_account_id = :accountId AND status = :status ORDER BY statement_date DESC")
    Flux<BankStatement> findByBankAccountIdAndStatus(UUID accountId, String status);

    @Query("SELECT * FROM treasury.bank_statements WHERE status = :status ORDER BY statement_date DESC")
    Flux<BankStatement> findByStatus(String status);

    @Query("SELECT * FROM treasury.bank_statements WHERE bank_account_id = :accountId AND period_start <= :date AND period_end >= :date")
    Mono<BankStatement> findByAccountIdAndPeriodContaining(UUID accountId, LocalDate date);

    @Query("SELECT * FROM treasury.bank_statements WHERE bank_account_id = :accountId AND statement_date BETWEEN :startDate AND :endDate ORDER BY statement_date DESC")
    Flux<BankStatement> findByAccountIdAndDateRange(UUID accountId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(*) > 0 FROM treasury.bank_statements WHERE bank_account_id = :accountId AND period_start = :periodStart AND period_end = :periodEnd")
    Mono<Boolean> existsByAccountIdAndPeriod(UUID accountId, LocalDate periodStart, LocalDate periodEnd);
}
