package com.rtcomops.treasury.application.mapper;

import com.rtcomops.treasury.application.dto.request.CreateStatementLineRequest;
import com.rtcomops.treasury.application.dto.response.StatementLineResponse;
import com.rtcomops.treasury.domain.model.StatementLine;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper for converting between StatementLine domain model and DTOs.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Component
public class StatementLineMapper {

    private static final String DEFAULT_STATUS = "UNMATCHED";

    /**
     * Converts a CreateStatementLineRequest to a StatementLine domain model.
     *
     * @param request the create request
     * @param lineNumber the line number (auto-generated if not provided)
     * @return the domain model
     */
    public StatementLine toDomain(CreateStatementLineRequest request, Integer lineNumber) {
        LocalDateTime now = LocalDateTime.now();

        return StatementLine.builder()
            .id(UUID.randomUUID())
            .bankStatementId(request.getBankStatementId())
            .lineNumber(request.getLineNumber() != null ? request.getLineNumber() : lineNumber)
            .transactionDate(request.getTransactionDate())
            .valueDate(request.getValueDate() != null ? request.getValueDate() : request.getTransactionDate())
            .amount(request.getAmount())
            .direction(request.getDirection().toUpperCase())
            .reference(request.getReference())
            .description(request.getDescription())
            .partnerName(request.getPartnerName())
            .partnerAccount(request.getPartnerAccount())
            .balanceAfter(request.getBalanceAfter())
            .reconciliationStatus(DEFAULT_STATUS)
            .rawData(request.getRawData())
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    /**
     * Converts a StatementLine domain model to a response DTO.
     *
     * @param domain the domain model
     * @return the response DTO
     */
    public StatementLineResponse toResponse(StatementLine domain) {
        return StatementLineResponse.builder()
            .id(domain.getId())
            .bankStatementId(domain.getBankStatementId())
            .lineNumber(domain.getLineNumber())
            .transactionDate(domain.getTransactionDate())
            .valueDate(domain.getValueDate())
            .amount(domain.getAmount())
            .direction(domain.getDirection())
            .reference(domain.getReference())
            .description(domain.getDescription())
            .partnerName(domain.getPartnerName())
            .partnerAccount(domain.getPartnerAccount())
            .balanceAfter(domain.getBalanceAfter())
            .reconciliationStatus(domain.getReconciliationStatus())
            .reconciledAt(domain.getReconciledAt())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .build();
    }

    /**
     * Marks a statement line as matched.
     *
     * @param line the statement line to mark
     * @return a new StatementLine with MATCHED status
     */
    public StatementLine markAsMatched(StatementLine line) {
        return line.toBuilder()
            .reconciliationStatus("MATCHED")
            .reconciledAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    /**
     * Marks a statement line as unmatched.
     *
     * @param line the statement line to mark
     * @return a new StatementLine with UNMATCHED status
     */
    public StatementLine markAsUnmatched(StatementLine line) {
        return line.toBuilder()
            .reconciliationStatus("UNMATCHED")
            .reconciledAt(null)
            .updatedAt(LocalDateTime.now())
            .build();
    }
}
