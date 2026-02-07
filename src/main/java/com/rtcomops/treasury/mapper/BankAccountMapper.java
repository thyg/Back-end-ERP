package com.rtcomops.treasury.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtcomops.treasury.dto.request.CreateBankAccountRequest;
import com.rtcomops.treasury.dto.request.UpdateBankAccountRequest;
import com.rtcomops.treasury.dto.response.BankAccountResponse;
import com.rtcomops.treasury.entity.BankAccount;
import io.r2dbc.postgresql.codec.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class BankAccountMapper {

    private static final Logger LOG = LoggerFactory.getLogger(BankAccountMapper.class);
    private final ObjectMapper objectMapper;

    public BankAccountMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public BankAccount toEntity(CreateBankAccountRequest request) {
        LocalDateTime now = LocalDateTime.now();
        
        Json detailsJson = null;
        try {
            if (request.getDetails() != null && !request.getDetails().isEmpty()) {
                detailsJson = Json.of(objectMapper.writeValueAsString(request.getDetails()));
            }
        } catch (JsonProcessingException e) {
            LOG.error("Error serializing bank account details to JSON", e);
        }

        return BankAccount.builder()
            .id(UUID.randomUUID())
            .bankId(request.getBankId())
            .accountTypeId(request.getAccountTypeId())
            .connectorTypeId(request.getConnectorTypeId())
            .name(request.getName())
            .currency(request.getCurrency() != null ? request.getCurrency() : "XAF")
            .currentBalance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO)
            .reconciledBalance(BigDecimal.ZERO)
            .isActive(request.getIsActive() != null ? request.getIsActive() : true)
            .details(detailsJson)
            .createdAt(now)
            .updatedAt(now)
            .isNew(true)
            .build();
    }
    
    public BankAccount updateEntity(BankAccount existing, UpdateBankAccountRequest request) {
        if (request.getBankId() != null) {
            existing.setBankId(request.getBankId());
        }
        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }
        // La mise à jour des détails est une logique complexe que nous ajoutons plus tard.
        // L'important est de ne plus toucher aux anciennes colonnes.
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setNew(false);
        return existing;
    }

    public BankAccountResponse toResponse(BankAccount entity) {
        Map<String, Object> detailsMap = null;
        try {
            if (entity.getDetails() != null) {
                detailsMap = objectMapper.readValue(entity.getDetails().asString(), new TypeReference<>() {});
            }
        } catch (JsonProcessingException e) {
            LOG.error("Error deserializing bank account details from JSON for accountId: {}", entity.getId(), e);
        }

        return BankAccountResponse.builder()
            .id(entity.getId())
            .bankId(entity.getBankId())
            .name(entity.getName())
            // On lit les informations depuis le 'detailsMap'
            .accountNumber(detailsMap != null ? (String) detailsMap.get("accountNumber") : null)
            .iban(detailsMap != null ? (String) detailsMap.get("iban") : null)
            .bic(detailsMap != null ? (String) detailsMap.get("bic") : null)
            .details(detailsMap) // On envoie le map complet pour le frontend
            .currency(entity.getCurrency())
            .currentBalance(entity.getCurrentBalance())
            .reconciledBalance(entity.getReconciledBalance())
            .isActive(entity.getIsActive())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    public BankAccountResponse toResponseWithBankName(BankAccount entity, String bankName) {
        BankAccountResponse response = toResponse(entity);
        response.setBankName(bankName);
        return response;
    }
}