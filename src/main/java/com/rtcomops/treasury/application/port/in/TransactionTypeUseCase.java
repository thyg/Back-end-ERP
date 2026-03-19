package com.rtcomops.treasury.application.port.in;

import com.rtcomops.treasury.application.dto.request.CreateTransactionTypeRequest;
import com.rtcomops.treasury.application.dto.request.UpdateTransactionTypeRequest;
import com.rtcomops.treasury.application.dto.response.TransactionTypeResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Input port for TransactionType use cases.
 *
 * <p>Defines the contract for transaction type business operations.
 * This port is implemented by the domain service and used by the infrastructure layer.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
public interface TransactionTypeUseCase {

    /**
     * Retrieves all transaction types ordered by code.
     *
     * @param activeOnly if true, returns only active transaction types
     * @return Flux of TransactionTypeResponse DTOs
     */
    Flux<TransactionTypeResponse> findAll(boolean activeOnly);

    /**
     * Retrieves a transaction type by its ID.
     *
     * @param id the transaction type ID
     * @return Mono of TransactionTypeResponse
     */
    Mono<TransactionTypeResponse> findById(UUID id);

    /**
     * Retrieves transaction types by category.
     *
     * @param category the category to filter by (BANK, CASH, CHECK, OTHER)
     * @return Flux of TransactionTypeResponse DTOs
     */
    Flux<TransactionTypeResponse> findByCategory(String category);

    /**
     * Creates a new transaction type.
     *
     * @param request the create request DTO
     * @return Mono of created TransactionTypeResponse
     */
    Mono<TransactionTypeResponse> create(CreateTransactionTypeRequest request);

    /**
     * Updates an existing transaction type.
     *
     * @param id the transaction type ID to update
     * @param request the update request DTO
     * @return Mono of updated TransactionTypeResponse
     */
    Mono<TransactionTypeResponse> update(UUID id, UpdateTransactionTypeRequest request);

    /**
     * Deletes a transaction type by its ID.
     *
     * @param id the transaction type ID to delete
     * @return Mono that completes when deletion is done
     */
    Mono<Void> delete(UUID id);
}
