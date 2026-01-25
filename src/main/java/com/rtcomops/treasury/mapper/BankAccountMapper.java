package com.rtcomops.treasury.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtcomops.treasury.dto.request.CreateBankAccountRequest;
import com.rtcomops.treasury.dto.request.UpdateBankAccountRequest;
import com.rtcomops.treasury.dto.response.BankAccountResponse;
import com.rtcomops.treasury.entity.BankAccount;
import io.r2dbc.postgresql.codec.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.type.TypeReference;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Map; // Import pour Map

@Component
public class BankAccountMapper {

    // --- CORRECTION 1 : Déclarer les champs 'final' ---
    private static final Logger LOG = LoggerFactory.getLogger(BankAccountMapper.class);
    private final ObjectMapper objectMapper;

    // Le constructeur est correct
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
            .accountSubTypeId(request.getAccountSubTypeId())
            .connectorTypeId(request.getConnectorTypeId())
            .name(request.getName())
            .branchCode(request.getBranchCode())
            .accountNumber(request.getAccountNumber())
            .generatedIban(request.getGeneratedIban())
            .currency(request.getCurrency() != null ? request.getCurrency() : "XAF")
            .currentBalance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO)
            .reconciledBalance(BigDecimal.ZERO)
            .isActive(request.getIsActive() != null ? request.getIsActive() : true)
            .overdraftAuthorized(request.getOverdraftAllowed() != null ? request.getOverdraftAllowed() : false)
            .overdraftLimit(request.getOverdraftLimit())
            .details(detailsJson)
            .createdAt(now)
            .updatedAt(now)
            .isNew(true)
            .build();
    }
    
    // --- CORRECTION 2 : La méthode updateEntity doit aussi gérer le champ 'details' ---
    public BankAccount updateEntity(BankAccount existing, UpdateBankAccountRequest request) {
        // ... (les mises à jour des autres champs restent)
        
        // NOTE : Pour l'instant, nous ne mettons pas à jour le champ 'details' via 'update'.
        // C'est une logique plus complexe que nous pourrons ajouter plus tard.
        // La méthode actuelle est conçue pour l'ancien modèle. Nous la laissons comme ça
        // pour éviter de casser le code existant, mais elle devra être refondue.

        if (request.getBankId() != null) existing.setBankId(request.getBankId());
        if (request.getName() != null) existing.setName(request.getName());
        // Les champs 'accountNumber', 'iban', 'bic' n'existent plus dans l'entité principale
        // if (request.getAccountNumber() != null) existing.setAccountNumber(request.getAccountNumber());
        // if (request.getIban() != null) existing.setIban(request.getIban());
        // if (request.getBic() != null) existing.setBic(request.getBic());
        if (request.getIsActive() != null) existing.setIsActive(request.getIsActive());

        existing.setUpdatedAt(LocalDateTime.now());
        existing.setNew(false);
        
        return existing;
    }

    // --- CORRECTION 3 : La méthode toResponse doit lire le champ 'details' ---
    public BankAccountResponse toResponse(BankAccount entity) {
        Map<String, Object> detailsMap = null;
        try {
            if (entity.getDetails() != null) {
                detailsMap = objectMapper.readValue(entity.getDetails().asString(), Map.class);
            }
        } catch (JsonProcessingException e) {
            LOG.error("Error deserializing bank account details from JSON", e);
        }

        return BankAccountResponse.builder()
            .id(entity.getId())
            .bankId(entity.getBankId())
            .name(entity.getName())
            .branchCode(entity.getBranchCode())
            .accountNumber(entity.getAccountNumber())
            .generatedIban(entity.getGeneratedIban())
            // On essaie de lire les anciennes colonnes du JSON pour la compatibilité
            .iban(detailsMap != null ? (String) detailsMap.get("iban") : entity.getIban())
            .bic(detailsMap != null ? (String) detailsMap.get("bic") : entity.getBic())
            .details(detailsMap)
            .currency(entity.getCurrency())
            .currentBalance(entity.getCurrentBalance())
            .reconciledBalance(entity.getReconciledBalance())
            .isActive(entity.getIsActive())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
    
    // --- CORRECTION 4 : Créez aussi le DTO de réponse ---
    // Vous aurez besoin de `BankAccountResponse.java` dans le package `dto/response`
    
    public BankAccountResponse toResponseWithBankName(BankAccount entity, String bankName) {
        BankAccountResponse response = toResponse(entity);
        response.setBankName(bankName);
        return response;
    }
}