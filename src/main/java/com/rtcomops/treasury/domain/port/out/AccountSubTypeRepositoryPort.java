package com.rtcomops.treasury.domain.port.out;

import com.rtcomops.treasury.domain.model.AccountSubType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Output port for AccountSubType repository operations.
 *
 * <p>Defines the contract for persistence operations on AccountSubType entities.
 * This port is implemented by the infrastructure layer adapter.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
public interface AccountSubTypeRepositoryPort {

    /**
     * Finds an account sub-type by its unique identifier.
     *
     * @param id the account sub-type ID
     * @return Mono containing the account sub-type if found, empty otherwise
     */
    Mono<AccountSubType> findById(UUID id);

    /**
     * Finds all sub-types for a given account type.
     *
     * @param accountTypeId the parent account type ID
     * @return Flux of sub-types belonging to the account type
     */
    Flux<AccountSubType> findByAccountTypeId(UUID accountTypeId);

    /**
     * Finds all active sub-types for a given account type.
     *
     * @param accountTypeId the parent account type ID
     * @return Flux of active sub-types belonging to the account type
     */
    Flux<AccountSubType> findByAccountTypeIdAndIsActiveTrue(UUID accountTypeId);

    /**
     * Finds a sub-type by its code within a parent account type.
     *
     * @param accountTypeId the parent account type ID
     * @param code the sub-type code
     * @return Mono containing the sub-type if found, empty otherwise
     */
    Mono<AccountSubType> findByAccountTypeIdAndCode(UUID accountTypeId, String code);

    /**
     * Checks if a sub-type with the given code exists within a parent type.
     *
     * @param accountTypeId the parent account type ID
     * @param code the code to check
     * @return Mono containing true if exists, false otherwise
     */
    Mono<Boolean> existsByAccountTypeIdAndCode(UUID accountTypeId, String code);

    /**
     * Checks if a sub-type with the given code exists within a parent type, excluding a specific ID.
     *
     * @param accountTypeId the parent account type ID
     * @param code the code to check
     * @param id the ID to exclude from the check
     * @return Mono containing true if another sub-type has this code
     */
    Mono<Boolean> existsByAccountTypeIdAndCodeAndIdNot(UUID accountTypeId, String code, UUID id);

    /**
     * Finds all active sub-types ordered by display order.
     *
     * @return Flux of all active sub-types
     */
    Flux<AccountSubType> findAllActiveOrderByOrdre();

    /**
     * Finds all sub-types ordered by display order.
     *
     * @return Flux of all sub-types
     */
    Flux<AccountSubType> findAllOrderByOrdre();

    /**
     * Counts sub-types for a given account type.
     *
     * @param accountTypeId the parent account type ID
     * @return Mono containing the count
     */
    Mono<Long> countByAccountTypeId(UUID accountTypeId);

    /**
     * Deletes all sub-types for a given account type.
     *
     * @param accountTypeId the parent account type ID
     * @return Mono signaling completion
     */
    Mono<Void> deleteByAccountTypeId(UUID accountTypeId);

    /**
     * Saves an account sub-type (insert or update).
     *
     * @param accountSubType the account sub-type to save
     * @return Mono containing the saved account sub-type
     */
    Mono<AccountSubType> save(AccountSubType accountSubType);

    /**
     * Deletes an account sub-type.
     *
     * @param accountSubType the account sub-type to delete
     * @return Mono that completes when deletion is done
     */
    Mono<Void> delete(AccountSubType accountSubType);
}
