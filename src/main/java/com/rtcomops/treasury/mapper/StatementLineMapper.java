package com.rtcomops.treasury.mapper;

import com.rtcomops.treasury.dto.request.CreateStatementLineRequest;
import com.rtcomops.treasury.dto.response.StatementLineResponse;
import com.rtcomops.treasury.entity.StatementLine;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper for converting between StatementLine entity and DTOs.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Component
public class StatementLineMapper {

    private static final String DEFAULT_STATUS = "UNMATCHED";

    /**
     * Converts a CreateStatementLineRequest to a StatementLine entity.
     *
     * @param request the create request
     * @param lineNumber the line number (auto-generated if not provided)
     * @return the entity
     */
    public StatementLine toEntity(CreateStatementLineRequest request, Integer lineNumber) {
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
            .isNew(true)
            .build();
    }

    /**
     * Converts a StatementLine entity to a response DTO.
     *
     * @param entity the entity
     * @return the response DTO
     */
    public StatementLineResponse toResponse(StatementLine entity) {
        return StatementLineResponse.builder()
            .id(entity.getId())
            .bankStatementId(entity.getBankStatementId())
            .lineNumber(entity.getLineNumber())
            .transactionDate(entity.getTransactionDate())
            .valueDate(entity.getValueDate())
            .amount(entity.getAmount())
            .direction(entity.getDirection())
            .reference(entity.getReference())
            .description(entity.getDescription())
            .partnerName(entity.getPartnerName())
            .partnerAccount(entity.getPartnerAccount())
            .balanceAfter(entity.getBalanceAfter())
            .reconciliationStatus(entity.getReconciliationStatus())
            .reconciledAt(entity.getReconciledAt())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    /**
     * Marks a statement line as matched.
     *
     * @param line the statement line to update
     * @return the updated entity
     */
    public StatementLine markAsMatched(StatementLine line) {
        line.setReconciliationStatus("MATCHED");
        line.setReconciledAt(LocalDateTime.now());
        line.setUpdatedAt(LocalDateTime.now());
        line.setNew(false);
        return line;
    }

    /**
     * Marks a statement line as unmatched.
     *
     * @param line the statement line to update
     * @return the updated entity
     */
    public StatementLine markAsUnmatched(StatementLine line) {
        line.setReconciliationStatus("UNMATCHED");
        line.setReconciledAt(null);
        line.setUpdatedAt(LocalDateTime.now());
        line.setNew(false);
        return line;
    }

    /**
     * Marks a statement line as ignored.
     *
     * @param line the statement line to update
     * @return the updated entity
     */
    public StatementLine markAsIgnored(StatementLine line) {
        line.setReconciliationStatus("IGNORED");
        line.setUpdatedAt(LocalDateTime.now());
        line.setNew(false);
        return line;
    }
}
