package com.rtcomops.treasury.mapper;

import com.rtcomops.treasury.dto.request.CreateCheckRequest;
import com.rtcomops.treasury.dto.request.UpdateCheckRequest;
import com.rtcomops.treasury.dto.response.CheckResponse;
import com.rtcomops.treasury.entity.Check;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper for converting between Check entity and DTOs.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Component
public class CheckMapper {

    private static final String DEFAULT_STATUS = "PENDING";

    /**
     * Converts a CreateCheckRequest to a Check entity.
     * Note: checkNumber and amountInWords may be set later by the service
     * if checkbookId is provided.
     *
     * @param request the create request DTO
     * @return a new Check entity
     */
    public Check toEntity(CreateCheckRequest request) {
        LocalDateTime now = LocalDateTime.now();

        return Check.builder()
            .id(UUID.randomUUID())
            .bankAccountId(request.getBankAccountId())
            .checkbookId(request.getCheckbookId())
            .checkType(request.getCheckType().toUpperCase())
            .checkNumber(request.getCheckNumber())
            .amount(request.getAmount())
            .partnerName(request.getPartnerName())
            .issueDate(request.getIssueDate())
            .dueDate(request.getDueDate())
            .status(DEFAULT_STATUS)
            .description(request.getDescription())
            .createdAt(now)
            .updatedAt(now)
            .isNew(true)
            .build();
    }

    public Check updateEntity(Check existing, UpdateCheckRequest request) {
        if (request.getCheckNumber() != null) {
            existing.setCheckNumber(request.getCheckNumber());
        }
        if (request.getAmount() != null) {
            existing.setAmount(request.getAmount());
        }
        if (request.getPartnerName() != null) {
            existing.setPartnerName(request.getPartnerName());
        }
        if (request.getIssueDate() != null) {
            existing.setIssueDate(request.getIssueDate());
        }
        if (request.getDueDate() != null) {
            existing.setDueDate(request.getDueDate());
        }
        if (request.getDepositDate() != null) {
            existing.setDepositDate(request.getDepositDate());
        }
        if (request.getCashedDate() != null) {
            existing.setCashedDate(request.getCashedDate());
        }
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus().toUpperCase());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getRejectionReason() != null) {
            existing.setRejectionReason(request.getRejectionReason());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setNew(false);
        
        return existing;
    }

    public CheckResponse toResponse(Check entity) {
        return CheckResponse.builder()
            .id(entity.getId())
            .bankAccountId(entity.getBankAccountId())
            .checkbookId(entity.getCheckbookId())
            .checkType(entity.getCheckType())
            .checkNumber(entity.getCheckNumber())
            .amount(entity.getAmount())
            .amountInWords(entity.getAmountInWords())
            .partnerName(entity.getPartnerName())
            .issueDate(entity.getIssueDate())
            .dueDate(entity.getDueDate())
            .depositDate(entity.getDepositDate())
            .cashedDate(entity.getCashedDate())
            .status(entity.getStatus())
            .description(entity.getDescription())
            .rejectionReason(entity.getRejectionReason())
            .bankTransactionId(entity.getBankTransactionId())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    public CheckResponse toResponseWithAccountName(Check entity, String accountName, String currency) {
        CheckResponse response = toResponse(entity);
        response.setBankAccountName(accountName);
        response.setCurrency(currency);
        return response;
    }

    /**
     * Converts a Check entity to a CheckResponse with account name, currency and checkbook prefix.
     *
     * @param entity the check entity
     * @param accountName the bank account name
     * @param currency the bank account currency
     * @param checkbookPrefix the checkbook prefix (if any)
     * @return the response DTO with details
     */
    public CheckResponse toResponseWithDetails(Check entity, String accountName, String currency, String checkbookPrefix) {
        CheckResponse response = toResponse(entity);
        response.setBankAccountName(accountName);
        response.setCurrency(currency);
        response.setCheckbookPrefix(checkbookPrefix);
        return response;
    }
}
