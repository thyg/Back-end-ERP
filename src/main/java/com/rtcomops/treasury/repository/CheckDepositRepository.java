package com.rtcomops.treasury.repository;

import com.rtcomops.treasury.entity.CheckDeposit;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive repository for CheckDeposit entity operations.
 *
 * <p>Provides methods to manage batch check deposits (remises de cheques en lot).</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2026-02-16
 */
@Repository
public interface CheckDepositRepository extends R2dbcRepository<CheckDeposit, UUID> {

    /**
     * Finds all check deposits ordered by deposit date (most recent first).
     *
     * @return Flux of CheckDeposit
     */
    @Query("SELECT * FROM treasury.check_deposits ORDER BY deposit_date DESC, created_at DESC")
    Flux<CheckDeposit> findAllOrderByDateDesc();

    /**
     * Finds all check deposits for a specific bank account.
     *
     * @param bankAccountId the bank account ID
     * @return Flux of CheckDeposit
     */
    @Query("SELECT * FROM treasury.check_deposits WHERE bank_account_id = :bankAccountId ORDER BY deposit_date DESC")
    Flux<CheckDeposit> findByBankAccountId(UUID bankAccountId);

    /**
     * Finds all check deposits with a specific status.
     *
     * @param status the status to filter by (DEPOSITED, RECONCILED)
     * @return Flux of CheckDeposit
     */
    @Query("SELECT * FROM treasury.check_deposits WHERE status = :status ORDER BY deposit_date DESC")
    Flux<CheckDeposit> findByStatus(String status);

    /**
     * Finds a check deposit by its unique reference.
     *
     * @param reference the deposit reference (e.g., REM-202602-0001)
     * @return Mono of CheckDeposit
     */
    @Query("SELECT * FROM treasury.check_deposits WHERE reference = :reference")
    Mono<CheckDeposit> findByReference(String reference);

    /**
     * Finds all unreconciled check deposits (status = DEPOSITED).
     *
     * @return Flux of CheckDeposit
     */
    @Query("SELECT * FROM treasury.check_deposits WHERE status = 'DEPOSITED' ORDER BY deposit_date ASC")
    Flux<CheckDeposit> findUnreconciled();

    /**
     * Finds all unreconciled check deposits for a specific bank account.
     *
     * @param bankAccountId the bank account ID
     * @return Flux of CheckDeposit
     */
    @Query("SELECT * FROM treasury.check_deposits WHERE bank_account_id = :bankAccountId AND status = 'DEPOSITED' ORDER BY deposit_date ASC")
    Flux<CheckDeposit> findUnreconciledByAccountId(UUID bankAccountId);

    /**
     * Counts deposits by status.
     *
     * @param status the status to count
     * @return Mono of count
     */
    @Query("SELECT COUNT(*) FROM treasury.check_deposits WHERE status = :status")
    Mono<Integer> countByStatus(String status);

    /**
     * Sums total amount by status.
     *
     * @param status the status to sum
     * @return Mono of total amount
     */
    @Query("SELECT COALESCE(SUM(total_amount), 0) FROM treasury.check_deposits WHERE status = :status")
    Mono<java.math.BigDecimal> sumAmountByStatus(String status);
}
