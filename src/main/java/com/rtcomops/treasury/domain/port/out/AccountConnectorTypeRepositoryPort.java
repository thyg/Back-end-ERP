package com.rtcomops.treasury.domain.port.out;

import com.rtcomops.treasury.domain.model.AccountConnectorType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Output port for AccountConnectorType repository operations.
 *
 * <p>Defines the contract for persistence operations on AccountConnectorType entities.
 * This port is implemented by the infrastructure layer adapter.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
public interface AccountConnectorTypeRepositoryPort {

    /**
     * Finds an account connector type by its unique identifier.
     *
     * @param id the connector type ID
     * @return Mono containing the connector type if found, empty otherwise
     */
    Mono<AccountConnectorType> findById(UUID id);

    /**
     * Finds all connector types for a specific bank category.
     *
     * @param bankCategoryId the bank category ID
     * @return Flux of connector types for the specified category
     */
    Flux<AccountConnectorType> findByBankCategoryId(UUID bankCategoryId);

    /**
     * Finds all account connector types.
     *
     * @return Flux of all connector types
     */
    Flux<AccountConnectorType> findAll();

    /**
     * Saves an account connector type (insert or update).
     *
     * @param connectorType the connector type to save
     * @return Mono containing the saved connector type
     */
    Mono<AccountConnectorType> save(AccountConnectorType connectorType);

    /**
     * Deletes an account connector type.
     *
     * @param connectorType the connector type to delete
     * @return Mono that completes when deletion is done
     */
    Mono<Void> delete(AccountConnectorType connectorType);
}
