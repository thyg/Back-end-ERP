package com.rtcomops.treasury.domain.port.out;

import com.rtcomops.treasury.domain.model.BankCategory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Output port for BankCategory repository operations.
 *
 * <p>Defines the contract for persistence operations on BankCategory entities.
 * This port is implemented by the infrastructure layer adapter.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
public interface BankCategoryRepositoryPort {

    /**
     * Finds a bank category by its unique identifier.
     *
     * @param id the bank category ID
     * @return Mono containing the bank category if found, empty otherwise
     */
    Mono<BankCategory> findById(UUID id);

    /**
     * Finds a bank category by its unique code.
     *
     * @param code the code to search for
     * @return Mono containing the bank category if found, empty otherwise
     */
    Mono<BankCategory> findByCode(String code);

    /**
     * Finds all bank categories.
     *
     * @return Flux of all bank categories
     */
    Flux<BankCategory> findAll();

    /**
     * Saves a bank category (insert or update).
     *
     * @param bankCategory the bank category to save
     * @return Mono containing the saved bank category
     */
    Mono<BankCategory> save(BankCategory bankCategory);

    /**
     * Deletes a bank category.
     *
     * @param bankCategory the bank category to delete
     * @return Mono that completes when deletion is done
     */
    Mono<Void> delete(BankCategory bankCategory);
}
