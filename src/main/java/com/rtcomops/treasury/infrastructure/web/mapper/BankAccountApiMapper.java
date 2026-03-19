package com.rtcomops.treasury.infrastructure.web.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtcomops.treasury.application.dto.request.CreateBankAccountRequest;
import com.rtcomops.treasury.application.dto.request.UpdateBankAccountRequest;
import com.rtcomops.treasury.application.dto.response.BankAccountResponse;
import com.rtcomops.treasury.domain.model.BankAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Mapper for converting between BankAccount domain model and web DTOs.
 *
 * <p>This mapper is used by the web controller to convert between
 * request/response DTOs and the domain model.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Component
public class BankAccountApiMapper {

    private static final Logger LOG = LoggerFactory.getLogger(BankAccountApiMapper.class);

    private final ObjectMapper objectMapper;

    /**
     * Constructs the mapper with ObjectMapper for JSON conversion.
     *
     * @param objectMapper the Jackson ObjectMapper
     */
    public BankAccountApiMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Converts a CreateBankAccountRequest DTO to a BankAccount domain model.
     *
     * @param request the create request DTO
     * @return a BankAccount domain model ready for persistence
     */
    public BankAccount toEntity(CreateBankAccountRequest request) {
        if (request == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();

        return BankAccount.builder()
                .id(UUID.randomUUID())
                .bankId(request.getBankId())
                .accountTypeId(request.getAccountTypeId())
                .accountSubTypeId(request.getAccountSubTypeId())
                .connectorTypeId(request.getConnectorTypeId())
                .name(request.getName())
                .branchCode(request.getBranchCode())
                .generatedIban(request.getGeneratedIban())
                .currency(request.getCurrency() != null ? request.getCurrency() : "XAF")
                .currentBalance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO)
                .reconciledBalance(BigDecimal.ZERO)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .details(request.getDetails())
                .overdraftAuthorized(request.getOverdraftAllowed() != null ? request.getOverdraftAllowed() : false)
                .overdraftLimit(request.getOverdraftLimit())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Updates an existing BankAccount domain model with values from UpdateBankAccountRequest.
     *
     * @param existing the existing domain model
     * @param request the update request DTO
     * @return the updated BankAccount domain model
     */
    public BankAccount updateEntity(BankAccount existing, UpdateBankAccountRequest request) {
        if (request == null) {
            return existing;
        }

        if (request.getBankId() != null) {
            existing.setBankId(request.getBankId());
        }
        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        return existing;
    }

    /**
     * Converts a BankAccount domain model to a BankAccountResponse DTO.
     *
     * @param entity the BankAccount domain model
     * @return the response DTO
     */
    public BankAccountResponse toResponse(BankAccount entity) {
        if (entity == null) {
            return null;
        }

        Map<String, Object> detailsMap = entity.getDetails();

        return BankAccountResponse.builder()
                .id(entity.getId())
                .bankId(entity.getBankId())
                .name(entity.getName())
                .branchCode(entity.getBranchCode())
                .accountNumber(detailsMap != null ? (String) detailsMap.get("accountNumber") : null)
                .generatedIban(entity.getGeneratedIban())
                .iban(detailsMap != null ? (String) detailsMap.get("iban") : null)
                .bic(detailsMap != null ? (String) detailsMap.get("bic") : null)
                .details(detailsMap)
                .currency(entity.getCurrency())
                .currentBalance(entity.getCurrentBalance())
                .reconciledBalance(entity.getReconciledBalance())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Converts a BankAccount domain model to a BankAccountResponse DTO with bank name.
     *
     * @param entity the BankAccount domain model
     * @param bankName the bank name to include
     * @return the response DTO with bank name
     */
    public BankAccountResponse toResponseWithBankName(BankAccount entity, String bankName) {
        BankAccountResponse response = toResponse(entity);
        if (response != null) {
            response.setBankName(bankName);
        }
        return response;
    }
}
