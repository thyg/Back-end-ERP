package com.rtcomops.treasury.application.port.in;

import com.rtcomops.treasury.application.dto.request.BankCategoryRequest;
import com.rtcomops.treasury.application.dto.response.BankCategoryResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Input port for BankCategory use cases.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
public interface BankCategoryUseCase {

    /**
     * Retrieves all bank categories.
     *
     * @return Flux of BankCategoryResponse
     */
    Flux<BankCategoryResponse> findAll();

    /**
     * Retrieves a bank category by its ID.
     *
     * @param id the bank category ID
     * @return Mono of BankCategoryResponse
     */
    Mono<BankCategoryResponse> findById(UUID id);

    /**
     * Creates a new bank category.
     *
     * @param request the create request
     * @return Mono of created BankCategoryResponse
     */
    Mono<BankCategoryResponse> create(BankCategoryRequest request);

    /**
     * Updates an existing bank category.
     *
     * @param id the bank category ID
     * @param request the update request
     * @return Mono of updated BankCategoryResponse
     */
    Mono<BankCategoryResponse> update(UUID id, BankCategoryRequest request);

    /**
     * Deletes a bank category by its ID.
     *
     * @param id the bank category ID
     * @return Mono<Void>
     */
    Mono<Void> delete(UUID id);
}
