package com.rtcomops.treasury.application.port.in;

import com.rtcomops.treasury.application.dto.request.CreateAccountTypeRequest;
import com.rtcomops.treasury.application.dto.request.UpdateAccountTypeRequest;
import com.rtcomops.treasury.application.dto.response.AccountSubTypeResponse;
import com.rtcomops.treasury.application.dto.response.AccountTypeResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Input port for AccountType use cases.
 *
 * <p>Defines the contract for account type business operations.
 * This port is implemented by the domain service and used by the infrastructure layer.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
public interface AccountTypeUseCase {

    /**
     * Retrieves all account types ordered by display order.
     *
     * @param activeOnly if true, returns only active account types
     * @return Flux of AccountTypeResponse DTOs
     */
    Flux<AccountTypeResponse> findAll(boolean activeOnly);

    /**
     * Retrieves an account type by its ID.
     *
     * @param id the account type ID
     * @return Mono of AccountTypeResponse
     */
    Mono<AccountTypeResponse> findById(UUID id);

    /**
     * Retrieves an account type by its ID with sub-types included.
     *
     * @param id the account type ID
     * @return Mono of AccountTypeResponse with subTypes populated
     */
    Mono<AccountTypeResponse> findByIdWithSubTypes(UUID id);

    /**
     * Retrieves an account type by its code.
     *
     * @param code the account type code
     * @return Mono of AccountTypeResponse
     */
    Mono<AccountTypeResponse> findByCode(String code);

    /**
     * Retrieves account types that allow check emission.
     *
     * @return Flux of AccountTypeResponse DTOs
     */
    Flux<AccountTypeResponse> findCheckEmitters();

    /**
     * Retrieves account types that allow check reception.
     *
     * @return Flux of AccountTypeResponse DTOs
     */
    Flux<AccountTypeResponse> findCheckReceivers();

    /**
     * Retrieves account types that allow cash transactions.
     *
     * @return Flux of AccountTypeResponse DTOs
     */
    Flux<AccountTypeResponse> findCashEnabled();

    /**
     * Retrieves account types that allow overdraft.
     *
     * @return Flux of AccountTypeResponse DTOs
     */
    Flux<AccountTypeResponse> findOverdraftEnabled();

    /**
     * Creates a new account type.
     *
     * @param request the create request DTO
     * @return Mono of created AccountTypeResponse
     */
    Mono<AccountTypeResponse> create(CreateAccountTypeRequest request);

    /**
     * Updates an existing account type.
     *
     * @param id the account type ID to update
     * @param request the update request DTO
     * @return Mono of updated AccountTypeResponse
     */
    Mono<AccountTypeResponse> update(UUID id, UpdateAccountTypeRequest request);

    /**
     * Deletes an account type by its ID.
     *
     * @param id the account type ID to delete
     * @return Mono that completes when deletion is done
     */
    Mono<Void> delete(UUID id);

    /**
     * Retrieves sub-types for a given account type.
     *
     * @param accountTypeId the parent account type ID
     * @param activeOnly if true, returns only active sub-types
     * @return Flux of AccountSubTypeResponse DTOs
     */
    Flux<AccountSubTypeResponse> findSubTypes(UUID accountTypeId, boolean activeOnly);
}
