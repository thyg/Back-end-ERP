package com.rtcomops.treasury.application.port.in;

import com.rtcomops.treasury.application.dto.request.CreateCheckDepositRequest;
import com.rtcomops.treasury.application.dto.response.CheckDepositResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Input port (Use Case) for check deposit operations.
 *
 * <p>Defines the business use cases for managing batch check deposits (remises de cheques en lot).</p>
 *
 * <p>Workflow:</p>
 * <ol>
 *   <li>Create (PENDING) - Checks are assigned to the deposit but remain RECEIVED</li>
 *   <li>Confirm Deposit (DEPOSITED) - Checks are physically deposited at the bank</li>
 *   <li>Cash (CASHED) - Funds are received, bank transaction created</li>
 * </ol>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-13
 */
public interface CheckDepositUseCase {

    /**
     * Retrieves all check deposits.
     *
     * @return Flux of CheckDepositResponse
     */
    Flux<CheckDepositResponse> findAll();

    /**
     * Retrieves all check deposits for a specific bank account.
     *
     * @param bankAccountId the bank account ID
     * @return Flux of CheckDepositResponse
     */
    Flux<CheckDepositResponse> findByBankAccountId(UUID bankAccountId);

    /**
     * Retrieves a check deposit by its ID with full details including checks.
     *
     * @param id the deposit ID
     * @return Mono of CheckDepositResponse
     */
    Mono<CheckDepositResponse> findById(UUID id);

    /**
     * Retrieves all unreconciled check deposits (PENDING or DEPOSITED status).
     *
     * @return Flux of CheckDepositResponse
     */
    Flux<CheckDepositResponse> findUnreconciled();

    /**
     * Creates a new check deposit batch with status PENDING.
     *
     * <p>Workflow Step 1: The deposit is created and checks are assigned to it,
     * but their status remains RECEIVED. They become unavailable for other deposits.</p>
     *
     * @param request the create request containing check IDs and deposit date
     * @return Mono of CheckDepositResponse
     */
    Mono<CheckDepositResponse> createDeposit(CreateCheckDepositRequest request);

    /**
     * Confirms that the deposit has been physically deposited at the bank.
     *
     * <p>Workflow Step 2: The deposit transitions from PENDING to DEPOSITED,
     * and all linked checks transition to DEPOSITED status.</p>
     *
     * @param depositId the deposit ID
     * @param depositDate the date when the deposit was made at the bank
     * @return Mono of CheckDepositResponse
     */
    Mono<CheckDepositResponse> confirmDeposit(UUID depositId, LocalDate depositDate);

    /**
     * Marks the deposit as cashed (funds received in account).
     *
     * <p>Workflow Step 3: The deposit transitions from DEPOSITED to CASHED.
     * A single BankTransaction is created for the total amount, the account
     * balance is updated, and all linked checks transition to CASHED status.</p>
     *
     * @param depositId the deposit ID
     * @param cashedDate the date when funds were received
     * @return Mono of CheckDepositResponse
     */
    Mono<CheckDepositResponse> cashDeposit(UUID depositId, LocalDate cashedDate);

    /**
     * Cancels a PENDING deposit and releases the assigned checks.
     *
     * <p>Only deposits in PENDING status can be cancelled.
     * All assigned checks will have their checkDepositId cleared.</p>
     *
     * @param depositId the deposit ID
     * @return Mono<Void>
     */
    Mono<Void> cancelDeposit(UUID depositId);
}
