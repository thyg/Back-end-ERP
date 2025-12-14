package com.rtcomops.treasury.mapper;

import com.rtcomops.treasury.dto.request.CreateBankTransactionRequest;
import com.rtcomops.treasury.dto.request.UpdateBankTransactionRequest;
import com.rtcomops.treasury.dto.response.BankTransactionResponse;
import com.rtcomops.treasury.entity.BankTransaction;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper for converting between BankTransaction entity and DTOs.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Component
public class BankTransactionMapper {

    private static final String DEFAULT_STATUS = "DRAFT";

    public BankTransaction toEntity(CreateBankTransactionRequest request) {
        LocalDateTime now = LocalDateTime.now();
        
        return BankTransaction.builder()
            .id(UUID.randomUUID())
            .bankAccountId(request.getBankAccountId())
            .transactionTypeId(request.getTransactionTypeId())
            .reference(request.getReference())
            .transactionDate(request.getTransactionDate())
            .valueDate(request.getValueDate() != null ? request.getValueDate() : request.getTransactionDate())
            .amount(request.getAmount())
            .direction(request.getDirection().toUpperCase())
            .description(request.getDescription())
            .partnerName(request.getPartnerName())
            .status(DEFAULT_STATUS)
            .isReconciled(false)
            .createdAt(now)
            .updatedAt(now)
            .isNew(true)
            .build();
    }

    public BankTransaction updateEntity(BankTransaction existing, UpdateBankTransactionRequest request) {
        if (request.getTransactionTypeId() != null) {
            existing.setTransactionTypeId(request.getTransactionTypeId());
        }
        if (request.getReference() != null) {
            existing.setReference(request.getReference());
        }
        if (request.getTransactionDate() != null) {
            existing.setTransactionDate(request.getTransactionDate());
        }
        if (request.getValueDate() != null) {
            existing.setValueDate(request.getValueDate());
        }
        if (request.getAmount() != null) {
            existing.setAmount(request.getAmount());
        }
        if (request.getDirection() != null) {
            existing.setDirection(request.getDirection().toUpperCase());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getPartnerName() != null) {
            existing.setPartnerName(request.getPartnerName());
        }
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus().toUpperCase());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setNew(false);
        
        return existing;
    }

    public BankTransaction copy(BankTransaction original) {
        return BankTransaction.builder()
            .id(original.getId())
            .bankAccountId(original.getBankAccountId())
            .transactionTypeId(original.getTransactionTypeId())
            .reference(original.getReference())
            .transactionDate(original.getTransactionDate())
            .valueDate(original.getValueDate())
            .amount(original.getAmount())
            .direction(original.getDirection())
            .description(original.getDescription())
            .partnerName(original.getPartnerName())
            .status(original.getStatus())
            .isReconciled(original.getIsReconciled())
            .reconciledAt(original.getReconciledAt())
            .createdAt(original.getCreatedAt())
            .updatedAt(original.getUpdatedAt())
            .isNew(original.isNew())
            .build();
    }

    public BankTransactionResponse toResponse(BankTransaction entity) {
        return BankTransactionResponse.builder()
            .id(entity.getId())
            .bankAccountId(entity.getBankAccountId())
            .transactionTypeId(entity.getTransactionTypeId())
            .reference(entity.getReference())
            .transactionDate(entity.getTransactionDate())
            .valueDate(entity.getValueDate())
            .amount(entity.getAmount())
            .direction(entity.getDirection())
            .description(entity.getDescription())
            .partnerName(entity.getPartnerName())
            .status(entity.getStatus())
            .isReconciled(entity.getIsReconciled())
            .reconciledAt(entity.getReconciledAt())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    public BankTransactionResponse toResponseWithDetails(
            BankTransaction entity, 
            String accountName, 
            String typeCode, 
            String typeLabel) {
        BankTransactionResponse response = toResponse(entity);
        response.setBankAccountName(accountName);
        response.setTransactionTypeCode(typeCode);
        response.setTransactionTypeLabel(typeLabel);
        return response;
    }
}
