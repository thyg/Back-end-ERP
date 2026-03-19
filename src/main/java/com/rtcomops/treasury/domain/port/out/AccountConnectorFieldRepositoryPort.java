package com.rtcomops.treasury.domain.port.out;

import com.rtcomops.treasury.domain.model.AccountConnectorField;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Output port for AccountConnectorField repository operations.
 *
 * <p>Defines the contract for persistence operations on AccountConnectorField entities.
 * This port is implemented by the infrastructure layer adapter.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
public interface AccountConnectorFieldRepositoryPort {

    /**
     * Finds an account connector field by its unique identifier.
     *
     * @param id the field ID
     * @return Mono containing the field if found, empty otherwise
     */
    Mono<AccountConnectorField> findById(UUID id);

    /**
     * Finds all fields for a specific connector type.
     *
     * @param connectorTypeId the connector type ID
     * @return Flux of fields for the specified connector type
     */
    Flux<AccountConnectorField> findByConnectorTypeId(UUID connectorTypeId);

    /**
     * Finds all account connector fields.
     *
     * @return Flux of all fields
     */
    Flux<AccountConnectorField> findAll();

    /**
     * Saves an account connector field (insert or update).
     *
     * @param field the field to save
     * @return Mono containing the saved field
     */
    Mono<AccountConnectorField> save(AccountConnectorField field);

    /**
     * Deletes an account connector field.
     *
     * @param field the field to delete
     * @return Mono that completes when deletion is done
     */
    Mono<Void> delete(AccountConnectorField field);
}
