package com.rtcomops.treasury.application.mapper;

import com.rtcomops.treasury.application.dto.request.CreateBankStatementRequest;
import com.rtcomops.treasury.application.dto.request.UpdateBankStatementRequest;
import com.rtcomops.treasury.application.dto.response.BankStatementResponse;
import com.rtcomops.treasury.domain.model.BankStatement;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper for converting between BankStatement domain model and DTOs.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Component
public class BankStatementMapper {

    private static final String DEFAULT_STATUS = "IMPORTED";

    /**
     * Converts a CreateBankStatementRequest to a BankStatement domain model.
     *
     * @param request the create request
     * @return the domain model
     */
    public BankStatement toDomain(CreateBankStatementRequest request) {
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
            .build();
    }

    /**
     * Updates an existing BankStatement domain model with data from UpdateBankStatementRequest.
     *
     * @param existing the existing domain model
     * @param request the update request
     * @return the updated domain model
     */
    public BankStatement updateDomain(BankStatement existing, UpdateBankStatementRequest request) {
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

        return existing;
    }

    /**
     * Converts a BankStatement domain model to a response DTO.
     *
     * @param domain the domain model
     * @return the response DTO
     */
    public BankStatementResponse toResponse(BankStatement domain) {
        return BankStatementResponse.builder()
            .id(domain.getId())
            .bankAccountId(domain.getBankAccountId())
            .reference(domain.getReference())
            .statementDate(domain.getStatementDate())
            .periodStart(domain.getPeriodStart())
            .periodEnd(domain.getPeriodEnd())
            .openingBalance(domain.getOpeningBalance())
            .closingBalance(domain.getClosingBalance())
            .totalCredits(domain.getTotalCredits())
            .totalDebits(domain.getTotalDebits())
            .lineCount(domain.getLineCount())
            .reconciledCount(domain.getReconciledCount())
            .reconciliationProgress(domain.getReconciliationProgress())
            .status(domain.getStatus())
            .importSource(domain.getImportSource())
            .fileName(domain.getFileName())
            .notes(domain.getNotes())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .build();
    }

    /**
     * Converts a BankStatement domain model to a response DTO with account name.
     *
     * @param domain the domain model
     * @param accountName the bank account name
     * @return the response DTO
     */
    public BankStatementResponse toResponseWithAccountName(BankStatement domain, String accountName) {
        BankStatementResponse response = toResponse(domain);
        response.setBankAccountName(accountName);
        return response;
    }
}
