package com.rtcomops.treasury.domain.port.out;

import com.rtcomops.treasury.domain.model.TransactionType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Output port for TransactionType repository operations.
 *
 * <p>Defines the contract for persistence operations on TransactionType entities.
 * This port is implemented by the infrastructure layer adapter.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
public interface TransactionTypeRepositoryPort {

    /**
     * Finds a transaction type by its unique identifier.
     *
     * @param id the transaction type ID
     * @return Mono containing the transaction type if found, empty otherwise
     */
    Mono<TransactionType> findById(UUID id);

    /**
     * Finds a transaction type by its unique code.
     *
     * @param code the transaction type code to search for
     * @return Mono containing the transaction type if found, empty otherwise
     */
    Mono<TransactionType> findByCode(String code);

    /**
     * Checks if a transaction type with the given code exists.
     *
     * @param code the code to check
     * @return Mono containing true if exists, false otherwise
     */
    Mono<Boolean> existsByCode(String code);

    /**
     * Finds all active transaction types ordered by code.
     *
     * @return Flux of active transaction types
     */
    Flux<TransactionType> findAllActiveOrderByCode();

    /**
     * Finds all transaction types ordered by code.
     *
     * @return Flux of all transaction types
     */
    Flux<TransactionType> findAllOrderByCode();

    /**
     * Finds transaction types by category.
     *
     * @param category the category to filter by (BANK, CASH, CHECK, OTHER)
     * @return Flux of transaction types in the specified category
     */
    Flux<TransactionType> findByCategory(String category);

    /**
     * Checks if a transaction type with the given code exists, excluding a specific ID.
     *
     * @param code the code to check
     * @param id the ID to exclude from the check
     * @return Mono containing true if another transaction type has this code
     */
    Mono<Boolean> existsByCodeAndIdNot(String code, UUID id);

    /**
     * Saves a transaction type (insert or update).
     *
     * @param transactionType the transaction type to save
     * @return Mono containing the saved transaction type
     */
    Mono<TransactionType> save(TransactionType transactionType);

    /**
     * Deletes a transaction type.
     *
     * @param transactionType the transaction type to delete
     * @return Mono that completes when deletion is done
     */
    Mono<Void> delete(TransactionType transactionType);
}
