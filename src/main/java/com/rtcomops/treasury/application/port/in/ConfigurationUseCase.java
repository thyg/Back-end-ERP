package com.rtcomops.treasury.application.port.in;

import com.rtcomops.treasury.application.dto.response.AccountConnectorResponse;
import com.rtcomops.treasury.domain.model.AccountConnectorType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Input port for Configuration use cases.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
public interface ConfigurationUseCase {

    /**
     * Retrieves an account connector type with its dynamic fields.
     *
     * @param connectorTypeId the connector type ID
     * @return Mono of AccountConnectorResponse
     */
    Mono<AccountConnectorResponse> getConnectorTypeWithFields(UUID connectorTypeId);

    /**
     * Retrieves account connector types filtered by bank category.
     *
     * @param bankCategoryId the bank category ID
     * @return Flux of AccountConnectorType
     */
    Flux<AccountConnectorType> findByBankCategory(UUID bankCategoryId);
}
