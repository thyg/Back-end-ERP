package com.rtcomops.treasury.service;

import com.rtcomops.treasury.dto.response.AccountConnectorResponse;
import com.rtcomops.treasury.entity.AccountConnectorField;
import com.rtcomops.treasury.entity.AccountConnectorType;
import com.rtcomops.treasury.exception.ResourceNotFoundException;
import com.rtcomops.treasury.repository.AccountConnectorFieldRepository;
import com.rtcomops.treasury.repository.AccountConnectorTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ConfigurationService {

    private final AccountConnectorTypeRepository connectorTypeRepository;
    private final AccountConnectorFieldRepository connectorFieldRepository;

    public ConfigurationService(
            AccountConnectorTypeRepository connectorTypeRepository,
            AccountConnectorFieldRepository connectorFieldRepository) {
        this.connectorTypeRepository = connectorTypeRepository;
        this.connectorFieldRepository = connectorFieldRepository;
    }

    public Mono<AccountConnectorResponse> getConnectorTypeWithFields(UUID connectorTypeId) {
        return connectorTypeRepository.findById(connectorTypeId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("AccountConnectorType", connectorTypeId)))
            .flatMap(connectorType ->
                connectorFieldRepository.findByConnectorTypeId(connectorTypeId)
                    .collectList()
                    .map(fields -> new AccountConnectorResponse(connectorType, fields))
            );
    }

    public Flux<AccountConnectorType> findByBankCategory(UUID bankCategoryId) {
        return connectorTypeRepository.findByBankCategoryId(bankCategoryId);
    }
}