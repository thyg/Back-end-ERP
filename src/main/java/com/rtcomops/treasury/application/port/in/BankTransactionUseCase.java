package com.rtcomops.treasury.application.port.in;

import com.rtcomops.treasury.application.dto.request.CreateBankTransactionRequest;
import com.rtcomops.treasury.application.dto.request.UpdateBankTransactionRequest;
import com.rtcomops.treasury.application.dto.response.BankTransactionResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Input port for BankTransaction use cases.
 *
 * <p>Defines the contract for bank transaction business operations.
 * This port is implemented by the domain service.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
public interface BankTransactionUseCase {

    /**
     * Retrieves all bank transactions.
     *
     * @return Flux of all bank transaction responses
     */
    Flux<BankTransactionResponse> findAll();

    /**
     * Retrieves a bank transaction by its ID.
     *
     * @param id the transaction ID
     * @return Mono containing the transaction response if found
     */
    Mono<BankTransactionResponse> findById(UUID id);

    /**
     * Retrieves all transactions for a specific account.
     *
     * @param accountId the bank account ID
     * @return Flux of transactions for the account
     */
    Flux<BankTransactionResponse> findByAccountId(UUID accountId);

    /**
     * Retrieves transactions by account and date range.
     *
     * @param accountId the bank account ID
     * @param startDate the start date
     * @param endDate the end date
     * @return Flux of transactions in the date range
     */
    Flux<BankTransactionResponse> findByAccountIdAndDateRange(UUID accountId, LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves transactions by status.
     *
     * @param status the transaction status
     * @return Flux of transactions with the given status
     */
    Flux<BankTransactionResponse> findByStatus(String status);

    /**
     * Creates a new bank transaction.
     *
     * @param request the creation request
     * @return Mono containing the created transaction response
     */
    Mono<BankTransactionResponse> create(CreateBankTransactionRequest request);

    /**
     * Updates an existing bank transaction.
     *
     * @param id the transaction ID
     * @param request the update request
     * @return Mono containing the updated transaction response
     */
    Mono<BankTransactionResponse> update(UUID id, UpdateBankTransactionRequest request);

    /**
     * Deletes a bank transaction.
     *
     * @param id the transaction ID
     * @return Mono that completes when deletion is done
     */
    Mono<Void> delete(UUID id);

    /**
     * Validates a draft transaction.
     * This changes the status from DRAFT to VALIDATED and updates the account balance.
     *
     * @param id the transaction ID
     * @return Mono containing the validated transaction response
     */
    Mono<BankTransactionResponse> validate(UUID id);

    /**
     * Cancels a transaction.
     * If the transaction was validated, this reverses the balance update.
     *
     * @param id the transaction ID
     * @return Mono containing the cancelled transaction response
     */
    Mono<BankTransactionResponse> cancel(UUID id);
}
