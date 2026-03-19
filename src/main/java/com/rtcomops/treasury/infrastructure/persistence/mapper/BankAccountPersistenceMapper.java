package com.rtcomops.treasury.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtcomops.treasury.domain.model.BankAccount;
import com.rtcomops.treasury.infrastructure.persistence.entity.BankAccountEntity;
import io.r2dbc.postgresql.codec.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Mapper for converting between BankAccount domain model and BankAccountEntity persistence entity.
 *
 * <p>This mapper handles the conversion between the domain's Map<String, Object> for details
 * and the persistence layer's io.r2dbc.postgresql.codec.Json type.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Component
public class BankAccountPersistenceMapper {

    private static final Logger LOG = LoggerFactory.getLogger(BankAccountPersistenceMapper.class);

    private final ObjectMapper objectMapper;

    /**
     * Constructs the mapper with ObjectMapper for JSON conversion.
     *
     * @param objectMapper the Jackson ObjectMapper
     */
    public BankAccountPersistenceMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Converts a BankAccount domain model to a BankAccountEntity for INSERT.
     *
     * @param domain the domain model
     * @return the persistence entity with isNew=true
     */
    public BankAccountEntity toEntity(BankAccount domain) {
        if (domain == null) {
            return null;
        }

        return BankAccountEntity.builder()
                .id(domain.getId())
                .bankId(domain.getBankId())
                .accountTypeId(domain.getAccountTypeId())
                .accountSubTypeId(domain.getAccountSubTypeId())
                .connectorTypeId(domain.getConnectorTypeId())
                .name(domain.getName())
                .branchCode(domain.getBranchCode())
                .generatedIban(domain.getGeneratedIban())
                .currency(domain.getCurrency())
                .currentBalance(domain.getCurrentBalance())
                .reconciledBalance(domain.getReconciledBalance())
                .isActive(domain.getIsActive())
                .details(mapToJson(domain.getDetails()))
                .overdraftAuthorized(domain.getOverdraftAuthorized())
                .overdraftLimit(domain.getOverdraftLimit())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .isNew(true)
                .build();
    }

    /**
     * Converts a BankAccount domain model to a BankAccountEntity for UPDATE.
     *
     * @param domain the domain model
     * @return the persistence entity with isNew=false
     */
    public BankAccountEntity toEntityForUpdate(BankAccount domain) {
        if (domain == null) {
            return null;
        }

        return BankAccountEntity.builder()
                .id(domain.getId())
                .bankId(domain.getBankId())
                .accountTypeId(domain.getAccountTypeId())
                .accountSubTypeId(domain.getAccountSubTypeId())
                .connectorTypeId(domain.getConnectorTypeId())
                .name(domain.getName())
                .branchCode(domain.getBranchCode())
                .generatedIban(domain.getGeneratedIban())
                .currency(domain.getCurrency())
                .currentBalance(domain.getCurrentBalance())
                .reconciledBalance(domain.getReconciledBalance())
                .isActive(domain.getIsActive())
                .details(mapToJson(domain.getDetails()))
                .overdraftAuthorized(domain.getOverdraftAuthorized())
                .overdraftLimit(domain.getOverdraftLimit())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .isNew(false)
                .build();
    }

    /**
     * Converts a BankAccountEntity persistence entity to a BankAccount domain model.
     *
     * @param entity the persistence entity
     * @return the domain model
     */
    public BankAccount toDomain(BankAccountEntity entity) {
        if (entity == null) {
            return null;
        }

        return BankAccount.builder()
                .id(entity.getId())
                .bankId(entity.getBankId())
                .accountTypeId(entity.getAccountTypeId())
                .accountSubTypeId(entity.getAccountSubTypeId())
                .connectorTypeId(entity.getConnectorTypeId())
                .name(entity.getName())
                .branchCode(entity.getBranchCode())
                .generatedIban(entity.getGeneratedIban())
                .currency(entity.getCurrency())
                .currentBalance(entity.getCurrentBalance())
                .reconciledBalance(entity.getReconciledBalance())
                .isActive(entity.getIsActive())
                .details(jsonToMap(entity.getDetails()))
                .overdraftAuthorized(entity.getOverdraftAuthorized())
                .overdraftLimit(entity.getOverdraftLimit())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Converts a Map to a PostgreSQL Json object.
     *
     * @param map the map to convert
     * @return the Json object, or null if map is null or empty
     */
    private Json mapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        try {
            return Json.of(objectMapper.writeValueAsString(map));
        } catch (JsonProcessingException e) {
            LOG.error("Error serializing details map to JSON", e);
            return null;
        }
    }

    /**
     * Converts a PostgreSQL Json object to a Map.
     *
     * @param json the Json object to convert
     * @return the map, or null if json is null
     */
    private Map<String, Object> jsonToMap(Json json) {
        if (json == null) {
            return null;
        }

        try {
            return objectMapper.readValue(json.asString(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            LOG.error("Error deserializing JSON to details map", e);
            return null;
        }
    }
}
