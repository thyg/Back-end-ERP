package com.rtcomops.treasury.domain.port.out;

import com.rtcomops.treasury.domain.model.BankStatement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Output port for BankStatement repository operations.
 *
 * <p>Defines the contract for persistence operations on BankStatement entities.
 * This port is implemented by the infrastructure layer adapter.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
public interface BankStatementRepositoryPort {

    /**
     * Finds a bank statement by its unique identifier.
     *
     * @param id the statement ID
     * @return Mono containing the statement if found, empty otherwise
     */
    Mono<BankStatement> findById(UUID id);

    /**
     * Finds all bank statements ordered by date descending.
     *
     * @return Flux of all statements
     */
    Flux<BankStatement> findAllOrderByDateDesc();

    /**
     * Finds all bank statements for a specific bank account.
     *
     * @param accountId the bank account ID
     * @return Flux of statements for the account
     */
    Flux<BankStatement> findByBankAccountId(UUID accountId);

    /**
     * Finds all bank statements for a specific bank account with a specific status.
     *
     * @param accountId the bank account ID
     * @param status the statement status
     * @return Flux of matching statements
     */
    Flux<BankStatement> findByBankAccountIdAndStatus(UUID accountId, String status);

    /**
     * Finds all bank statements by status.
     *
     * @param status the statement status
     * @return Flux of statements with the specified status
     */
    Flux<BankStatement> findByStatus(String status);

    /**
     * Finds a bank statement by account ID and period containing a specific date.
     *
     * @param accountId the bank account ID
     * @param date the date to check
     * @return Mono containing the statement if found
     */
    Mono<BankStatement> findByAccountIdAndPeriodContaining(UUID accountId, LocalDate date);

    /**
     * Finds bank statements by account ID and date range.
     *
     * @param accountId the bank account ID
     * @param startDate the start date
     * @param endDate the end date
     * @return Flux of matching statements
     */
    Flux<BankStatement> findByAccountIdAndDateRange(UUID accountId, LocalDate startDate, LocalDate endDate);

    /**
     * Checks if a statement exists for the given account and period.
     *
     * @param accountId the bank account ID
     * @param periodStart the period start date
     * @param periodEnd the period end date
     * @return Mono<Boolean> true if exists
     */
    Mono<Boolean> existsByAccountIdAndPeriod(UUID accountId, LocalDate periodStart, LocalDate periodEnd);

    /**
     * Saves a bank statement (insert or update).
     *
     * @param statement the statement to save
     * @return Mono containing the saved statement
     */
    Mono<BankStatement> save(BankStatement statement);

    /**
     * Deletes a bank statement.
     *
     * @param statement the statement to delete
     * @return Mono that completes when deletion is done
     */
    Mono<Void> delete(BankStatement statement);
}
