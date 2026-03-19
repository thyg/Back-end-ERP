package com.rtcomops.treasury.application.port.in;

import com.rtcomops.treasury.application.dto.request.CreateBankAccountRequest;
import com.rtcomops.treasury.application.dto.request.UpdateBankAccountRequest;
import com.rtcomops.treasury.application.dto.response.BankAccountResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Input port for BankAccount use cases.
 *
 * <p>Defines the contract for bank account business operations.
 * This port is implemented by the domain service and used by the infrastructure layer.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
public interface BankAccountUseCase {

    /**
     * Retrieves all bank accounts ordered by name.
     *
     * @param activeOnly if true, returns only active bank accounts
     * @return Flux of BankAccountResponse DTOs
     */
    Flux<BankAccountResponse> findAll(boolean activeOnly);

    /**
     * Retrieves a bank account by its ID.
     *
     * @param id the bank account ID
     * @return Mono of BankAccountResponse
     */
    Mono<BankAccountResponse> findById(UUID id);

    /**
     * Retrieves all bank accounts for a specific bank.
     *
     * @param bankId the bank ID
     * @return Flux of BankAccountResponse DTOs
     */
    Flux<BankAccountResponse> findByBankId(UUID bankId);

    /**
     * Creates a new bank account.
     *
     * @param request the create request DTO
     * @return Mono of created BankAccountResponse
     */
    Mono<BankAccountResponse> create(CreateBankAccountRequest request);

    /**
     * Updates an existing bank account.
     *
     * @param id the bank account ID to update
     * @param request the update request DTO
     * @return Mono of updated BankAccountResponse
     */
    Mono<BankAccountResponse> update(UUID id, UpdateBankAccountRequest request);

    /**
     * Deletes a bank account by its ID.
     *
     * @param id the bank account ID to delete
     * @return Mono that completes when deletion is done
     */
    Mono<Void> delete(UUID id);
}
