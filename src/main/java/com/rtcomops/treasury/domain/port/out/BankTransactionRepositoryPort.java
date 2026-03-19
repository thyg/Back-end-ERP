package com.rtcomops.treasury.domain.port.out;

import com.rtcomops.treasury.domain.model.BankTransaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Output port for BankTransaction repository operations.
 *
 * <p>Defines the contract for persistence operations on BankTransaction entities.
 * This port is implemented by the infrastructure layer adapter.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
public interface BankTransactionRepositoryPort {

    /**
     * Finds all transactions ordered by date descending.
     *
     * @return Flux of all bank transactions
     */
    Flux<BankTransaction> findAllOrderByDateDesc();

    /**
     * Finds a bank transaction by its unique identifier.
     *
     * @param id the transaction ID
     * @return Mono containing the transaction if found, empty otherwise
     */
    Mono<BankTransaction> findById(UUID id);

    /**
     * Finds all transactions for a specific bank account.
     *
     * @param accountId the bank account ID
     * @return Flux of transactions for the account
     */
    Flux<BankTransaction> findByBankAccountId(UUID accountId);

    /**
     * Finds transactions by account and status.
     *
     * @param accountId the bank account ID
     * @param status the transaction status
     * @return Flux of matching transactions
     */
    Flux<BankTransaction> findByBankAccountIdAndStatus(UUID accountId, String status);

    /**
     * Finds transactions by account and date range.
     *
     * @param accountId the bank account ID
     * @param startDate the start date
     * @param endDate the end date
     * @return Flux of transactions in the date range
     */
    Flux<BankTransaction> findByBankAccountIdAndDateRange(UUID accountId, LocalDate startDate, LocalDate endDate);

    /**
     * Finds unreconciled transactions for an account.
     *
     * @param accountId the bank account ID
     * @return Flux of unreconciled transactions
     */
    Flux<BankTransaction> findUnreconciledByAccountId(UUID accountId);

    /**
     * Finds transactions by status.
     *
     * @param status the transaction status
     * @return Flux of transactions with the given status
     */
    Flux<BankTransaction> findByStatus(String status);

    /**
     * Checks if a transaction with the given reference exists.
     *
     * @param reference the reference to check
     * @return Mono containing true if exists, false otherwise
     */
    Mono<Boolean> existsByReference(String reference);

    /**
     * Saves a bank transaction (insert or update).
     *
     * @param transaction the transaction to save
     * @return Mono containing the saved transaction
     */
    Mono<BankTransaction> save(BankTransaction transaction);

    /**
     * Deletes a bank transaction.
     *
     * @param transaction the transaction to delete
     * @return Mono that completes when deletion is done
     */
    Mono<Void> delete(BankTransaction transaction);
}
