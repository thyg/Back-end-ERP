package com.rtcomops.treasury.mapper;

import com.rtcomops.treasury.dto.request.CreateBankStatementRequest;
import com.rtcomops.treasury.dto.request.UpdateBankStatementRequest;
import com.rtcomops.treasury.dto.response.BankStatementResponse;
import com.rtcomops.treasury.entity.BankStatement;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper for converting between BankStatement entity and DTOs.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Component
public class BankStatementMapper {

    private static final String DEFAULT_STATUS = "IMPORTED";

    /**
     * Converts a CreateBankStatementRequest to a BankStatement entity.
     *
     * @param request the create request
     * @return the entity
     */
    public BankStatement toEntity(CreateBankStatementRequest request) {
        LocalDateTime now = LocalDateTime.now();
        
        return BankStatement.builder()
            .id(UUID.randomUUID())
            .bankAccountId(request.getBankAccountId())
            .reference(request.getReference())
            .statementDate(request.getStatementDate())
            .periodStart(request.getPeriodStart())
            .periodEnd(request.getPeriodEnd())
            .openingBalance(request.getOpeningBalance())
            .closingBalance(request.getClosingBalance())
            .totalCredits(BigDecimal.ZERO)
            .totalDebits(BigDecimal.ZERO)
            .lineCount(0)
            .reconciledCount(0)
            .status(DEFAULT_STATUS)
            .importSource(request.getImportSource())
            .fileName(request.getFileName())
            .notes(request.getNotes())
            .createdAt(now)
            .updatedAt(now)
            .isNew(true)
            .build();
    }

    /**
     * Updates an existing BankStatement entity with data from UpdateBankStatementRequest.
     *
     * @param existing the existing entity
     * @param request the update request
     * @return the updated entity
     */
    public BankStatement updateEntity(BankStatement existing, UpdateBankStatementRequest request) {
        if (request.getReference() != null) {
            existing.setReference(request.getReference());
        }
        if (request.getStatementDate() != null) {
            existing.setStatementDate(request.getStatementDate());
        }
        if (request.getPeriodStart() != null) {
            existing.setPeriodStart(request.getPeriodStart());
        }
        if (request.getPeriodEnd() != null) {
            existing.setPeriodEnd(request.getPeriodEnd());
        }
        if (request.getOpeningBalance() != null) {
            existing.setOpeningBalance(request.getOpeningBalance());
        }
        if (request.getClosingBalance() != null) {
            existing.setClosingBalance(request.getClosingBalance());
        }
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus().toUpperCase());
        }
        if (request.getNotes() != null) {
            existing.setNotes(request.getNotes());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setNew(false);
        
        return existing;
    }

    /**
     * Converts a BankStatement entity to a response DTO.
     *
     * @param entity the entity
     * @return the response DTO
     */
    public BankStatementResponse toResponse(BankStatement entity) {
        BigDecimal progress = calculateProgress(entity.getLineCount(), entity.getReconciledCount());
        
        return BankStatementResponse.builder()
            .id(entity.getId())
            .bankAccountId(entity.getBankAccountId())
            .reference(entity.getReference())
            .statementDate(entity.getStatementDate())
            .periodStart(entity.getPeriodStart())
            .periodEnd(entity.getPeriodEnd())
            .openingBalance(entity.getOpeningBalance())
            .closingBalance(entity.getClosingBalance())
            .totalCredits(entity.getTotalCredits())
            .totalDebits(entity.getTotalDebits())
            .lineCount(entity.getLineCount())
            .reconciledCount(entity.getReconciledCount())
            .reconciliationProgress(progress)
            .status(entity.getStatus())
            .importSource(entity.getImportSource())
            .fileName(entity.getFileName())
            .notes(entity.getNotes())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    /**
     * Converts a BankStatement entity to a response DTO with account name.
     *
     * @param entity the entity
     * @param accountName the bank account name
     * @return the response DTO
     */
    public BankStatementResponse toResponseWithAccountName(BankStatement entity, String accountName) {
        BankStatementResponse response = toResponse(entity);
        response.setBankAccountName(accountName);
        return response;
    }

    /**
     * Calculates reconciliation progress percentage.
     *
     * @param lineCount total number of lines
     * @param reconciledCount number of reconciled lines
     * @return progress percentage (0-100)
     */
    private BigDecimal calculateProgress(Integer lineCount, Integer reconciledCount) {
        if (lineCount == null || lineCount == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(reconciledCount)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(lineCount), 2, RoundingMode.HALF_UP);
    }
}
