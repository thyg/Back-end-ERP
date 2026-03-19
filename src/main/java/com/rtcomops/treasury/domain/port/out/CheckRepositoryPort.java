package com.rtcomops.treasury.domain.port.out;

import com.rtcomops.treasury.domain.model.Check;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Output port for Check repository operations.
 *
 * <p>Defines the contract for persistence operations on Check entities.
 * This port is implemented by the infrastructure layer adapter.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
public interface CheckRepositoryPort {

    /**
     * Finds a check by its unique identifier.
     *
     * @param id the check ID
     * @return Mono containing the check if found, empty otherwise
     */
    Mono<Check> findById(UUID id);

    /**
     * Finds all checks by their IDs.
     *
     * @param ids the list of check IDs
     * @return Flux of checks found
     */
    Flux<Check> findAllById(java.util.List<UUID> ids);

    /**
     * Finds all checks ordered by date descending.
     *
     * @return Flux of all checks
     */
    Flux<Check> findAllOrderByDateDesc();

    /**
     * Finds all checks for a specific bank account.
     *
     * @param accountId the bank account ID
     * @return Flux of checks for the account
     */
    Flux<Check> findByBankAccountId(UUID accountId);

    /**
     * Finds all checks by check type.
     *
     * @param checkType the check type (ISSUED or RECEIVED)
     * @return Flux of checks with the specified type
     */
    Flux<Check> findByCheckType(String checkType);

    /**
     * Finds all checks by status.
     *
     * @param status the check status
     * @return Flux of checks with the specified status
     */
    Flux<Check> findByStatus(String status);

    /**
     * Finds all checks by check type and status.
     *
     * @param checkType the check type
     * @param status the check status
     * @return Flux of matching checks
     */
    Flux<Check> findByCheckTypeAndStatus(String checkType, String status);

    /**
     * Finds all checks for a bank account with a specific check type.
     *
     * @param accountId the bank account ID
     * @param checkType the check type
     * @return Flux of matching checks
     */
    Flux<Check> findByBankAccountIdAndCheckType(UUID accountId, String checkType);

    /**
     * Finds pending checks due before a specific date.
     *
     * @param date the date to compare against
     * @return Flux of pending checks due before the date
     */
    Flux<Check> findPendingChecksDueBefore(LocalDate date);

    /**
     * Checks if a check exists with the given number, account and type.
     *
     * @param checkNumber the check number
     * @param accountId the bank account ID
     * @param checkType the check type
     * @return Mono<Boolean> true if exists
     */
    Mono<Boolean> existsByCheckNumberAndAccountAndType(String checkNumber, UUID accountId, String checkType);

    /**
     * Checks if a check exists with the given number, account and type, excluding a specific ID.
     *
     * @param checkNumber the check number
     * @param accountId the bank account ID
     * @param checkType the check type
     * @param id the ID to exclude
     * @return Mono<Boolean> true if exists
     */
    Mono<Boolean> existsByCheckNumberAndAccountAndTypeAndIdNot(String checkNumber, UUID accountId, String checkType, UUID id);

    /**
     * Finds all checks belonging to a specific checkbook.
     *
     * @param checkbookId the checkbook ID
     * @return Flux of checks from that checkbook
     */
    Flux<Check> findByCheckbookId(UUID checkbookId);

    /**
     * Finds all checks belonging to a specific check deposit.
     *
     * @param checkDepositId the check deposit ID
     * @return Flux of checks in that deposit
     */
    Flux<Check> findByCheckDepositId(UUID checkDepositId);

    /**
     * Finds all overdue checks.
     *
     * @return Flux of overdue checks
     */
    Flux<Check> findOverdueChecks();

    /**
     * Saves a check (insert or update).
     *
     * @param check the check to save
     * @return Mono containing the saved check
     */
    Mono<Check> save(Check check);

    /**
     * Deletes a check.
     *
     * @param check the check to delete
     * @return Mono that completes when deletion is done
     */
    Mono<Void> delete(Check check);

    // =========================================================================
    // STATISTICS METHODS
    // =========================================================================

    /**
     * Counts checks by status.
     *
     * @param status the status to count
     * @return Mono containing the count
     */
    Mono<Integer> countByStatus(String status);

    /**
     * Sums amounts by status.
     *
     * @param status the status to sum
     * @return Mono containing the sum
     */
    Mono<BigDecimal> sumAmountByStatus(String status);

    /**
     * Counts checks by type (excluding cancelled).
     *
     * @param checkType the check type
     * @return Mono containing the count
     */
    Mono<Integer> countByCheckType(String checkType);

    /**
     * Sums amounts by type (excluding cancelled).
     *
     * @param checkType the check type
     * @return Mono containing the sum
     */
    Mono<BigDecimal> sumAmountByCheckType(String checkType);

    /**
     * Counts all checks excluding cancelled.
     *
     * @return Mono containing the count
     */
    Mono<Integer> countAllExcludingCancelled();

    /**
     * Sums total amount excluding cancelled.
     *
     * @return Mono containing the sum
     */
    Mono<BigDecimal> sumTotalAmountExcludingCancelled();

    /**
     * Counts overdue checks.
     *
     * @return Mono containing the count
     */
    Mono<Integer> countOverdueChecks();

    /**
     * Sums amount of overdue checks.
     *
     * @return Mono containing the sum
     */
    Mono<BigDecimal> sumOverdueAmount();
}
