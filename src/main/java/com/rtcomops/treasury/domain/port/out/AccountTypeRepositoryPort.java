package com.rtcomops.treasury.domain.port.out;

import com.rtcomops.treasury.domain.model.AccountType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Output port for AccountType repository operations.
 *
 * <p>Defines the contract for persistence operations on AccountType entities.
 * This port is implemented by the infrastructure layer adapter.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
public interface AccountTypeRepositoryPort {

    /**
     * Finds an account type by its unique identifier.
     *
     * @param id the account type ID
     * @return Mono containing the account type if found, empty otherwise
     */
    Mono<AccountType> findById(UUID id);

    /**
     * Finds an account type by its unique code.
     *
     * @param code the account type code to search for
     * @return Mono containing the account type if found, empty otherwise
     */
    Mono<AccountType> findByCode(String code);

    /**
     * Checks if an account type with the given code exists.
     *
     * @param code the code to check
     * @return Mono containing true if exists, false otherwise
     */
    Mono<Boolean> existsByCode(String code);

    /**
     * Finds all active account types ordered by display order.
     *
     * @return Flux of active account types
     */
    Flux<AccountType> findAllActiveOrderByOrdre();

    /**
     * Finds all account types ordered by display order.
     *
     * @return Flux of all account types
     */
    Flux<AccountType> findAllOrderByOrdre();

    /**
     * Finds account types that allow check emission.
     *
     * @return Flux of account types that can emit checks
     */
    Flux<AccountType> findByPeutEmettreChequesTrue();

    /**
     * Finds account types that allow check reception.
     *
     * @return Flux of account types that can receive checks
     */
    Flux<AccountType> findByPeutRecevoirChequesTrue();

    /**
     * Finds account types that allow cash transactions.
     *
     * @return Flux of account types that allow cash
     */
    Flux<AccountType> findByPeutTransactionsEspecesTrue();

    /**
     * Finds account types that allow overdraft.
     *
     * @return Flux of account types with overdraft authorized
     */
    Flux<AccountType> findByDecouvertAutoriseTrue();

    /**
     * Checks if an account type with the given code exists, excluding a specific ID.
     *
     * @param code the code to check
     * @param id the ID to exclude from the check
     * @return Mono containing true if another account type has this code
     */
    Mono<Boolean> existsByCodeAndIdNot(String code, UUID id);

    /**
     * Saves an account type (insert or update).
     *
     * @param accountType the account type to save
     * @return Mono containing the saved account type
     */
    Mono<AccountType> save(AccountType accountType);

    /**
     * Deletes an account type.
     *
     * @param accountType the account type to delete
     * @return Mono that completes when deletion is done
     */
    Mono<Void> delete(AccountType accountType);
}
