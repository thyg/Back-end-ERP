package com.rtcomops.treasury.repository;

import com.rtcomops.treasury.entity.TransactionType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive repository for TransactionType entity operations.
 *
 * <p>Provides non-blocking CRUD operations and custom queries
 * for the treasury.transaction_types table using R2DBC.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@Repository
public interface TransactionTypeRepository extends R2dbcRepository<TransactionType, UUID> {

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
    @Query("SELECT * FROM treasury.transaction_types WHERE is_active = true ORDER BY code ASC")
    Flux<TransactionType> findAllActiveOrderByCode();

    /**
     * Finds all transaction types ordered by code.
     *
     * @return Flux of all transaction types
     */
    @Query("SELECT * FROM treasury.transaction_types ORDER BY code ASC")
    Flux<TransactionType> findAllOrderByCode();

    /**
     * Finds transaction types by category.
     *
     * @param category the category to filter by (BANK, CASH, CHECK, OTHER)
     * @return Flux of transaction types in the specified category
     */
    @Query("SELECT * FROM treasury.transaction_types WHERE category = :category AND is_active = true ORDER BY code ASC")
    Flux<TransactionType> findByCategory(String category);

    /**
     * Checks if a transaction type with the given code exists, excluding a specific ID.
     *
     * @param code the code to check
     * @param id the ID to exclude from the check
     * @return Mono containing true if another transaction type has this code
     */
    @Query("SELECT COUNT(*) > 0 FROM treasury.transaction_types WHERE code = :code AND id != :id")
    Mono<Boolean> existsByCodeAndIdNot(String code, UUID id);
}
