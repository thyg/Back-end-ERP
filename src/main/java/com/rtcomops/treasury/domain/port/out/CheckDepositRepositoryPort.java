package com.rtcomops.treasury.domain.port.out;

import com.rtcomops.treasury.domain.model.CheckDeposit;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Output port for CheckDeposit repository operations.
 *
 * <p>Defines the contract for persistence operations on CheckDeposit entities.
 * This port is implemented by the infrastructure layer adapter.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2026-02-16
 */
public interface CheckDepositRepositoryPort {

    /**
     * Finds a check deposit by its unique identifier.
     *
     * @param id the deposit ID
     * @return Mono containing the deposit if found, empty otherwise
     */
    Mono<CheckDeposit> findById(UUID id);

    /**
     * Finds all check deposits ordered by date descending.
     *
     * @return Flux of all deposits
     */
    Flux<CheckDeposit> findAllOrderByDateDesc();

    /**
     * Finds all check deposits for a specific bank account.
     *
     * @param bankAccountId the bank account ID
     * @return Flux of deposits for the account
     */
    Flux<CheckDeposit> findByBankAccountId(UUID bankAccountId);

    /**
     * Finds all check deposits with a specific status.
     *
     * @param status the status to filter by
     * @return Flux of deposits with the specified status
     */
    Flux<CheckDeposit> findByStatus(String status);

    /**
     * Finds a check deposit by its unique reference.
     *
     * @param reference the deposit reference
     * @return Mono containing the deposit if found
     */
    Mono<CheckDeposit> findByReference(String reference);

    /**
     * Finds all unreconciled check deposits.
     *
     * @return Flux of unreconciled deposits
     */
    Flux<CheckDeposit> findUnreconciled();

    /**
     * Finds all unreconciled check deposits for a specific bank account.
     *
     * @param bankAccountId the bank account ID
     * @return Flux of unreconciled deposits
     */
    Flux<CheckDeposit> findUnreconciledByAccountId(UUID bankAccountId);

    /**
     * Counts deposits by status.
     *
     * @param status the status to count
     * @return Mono containing the count
     */
    Mono<Integer> countByStatus(String status);

    /**
     * Sums total amount by status.
     *
     * @param status the status to sum
     * @return Mono containing the total amount
     */
    Mono<BigDecimal> sumAmountByStatus(String status);

    /**
     * Saves a check deposit (insert or update).
     *
     * @param deposit the deposit to save
     * @return Mono containing the saved deposit
     */
    Mono<CheckDeposit> save(CheckDeposit deposit);

    /**
     * Deletes a check deposit.
     *
     * @param deposit the deposit to delete
     * @return Mono that completes when deletion is done
     */
    Mono<Void> delete(CheckDeposit deposit);
}
