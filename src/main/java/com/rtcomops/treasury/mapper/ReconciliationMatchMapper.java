package com.rtcomops.treasury.mapper;

import com.rtcomops.treasury.dto.request.ReconcileManualRequest;
import com.rtcomops.treasury.dto.response.ReconciliationMatchResponse;
import com.rtcomops.treasury.entity.ReconciliationMatch;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper for converting between ReconciliationMatch entity and DTOs.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Component
public class ReconciliationMatchMapper {

    /**
     * Creates a ReconciliationMatch entity from a manual reconciliation request.
     *
     * @param request the manual reconciliation request
     * @param matchedAmount the matched amount
     * @return the entity
     */
    public ReconciliationMatch toEntityFromManual(ReconcileManualRequest request, BigDecimal matchedAmount) {
        String matchType = determineMatchType(request);
        
        return ReconciliationMatch.builder()
            .id(UUID.randomUUID())
            .statementLineId(request.getStatementLineId())
            .bankTransactionId(request.getBankTransactionId())
            .checkId(request.getCheckId())
            .matchType(matchType)
            .matchMethod("MANUAL")
            .matchedAmount(request.getMatchedAmount() != null ? request.getMatchedAmount() : matchedAmount)
            .confidenceScore(BigDecimal.valueOf(100))
            .notes(request.getNotes())
            .matchedBy(request.getMatchedBy())
            .createdAt(LocalDateTime.now())
            .isNew(true)
            .build();
    }

    /**
     * Creates a ReconciliationMatch entity for automatic matching.
     *
     * @param statementLineId the statement line ID
     * @param transactionId the bank transaction ID (nullable)
     * @param checkId the check ID (nullable)
     * @param matchedAmount the matched amount
     * @param confidenceScore the confidence score (0-100)
     * @param matchMethod the match method (AUTO_EXACT, AUTO_FUZZY, RULE_BASED)
     * @return the entity
     */
    public ReconciliationMatch toEntityFromAuto(
            UUID statementLineId,
            UUID transactionId,
            UUID checkId,
            BigDecimal matchedAmount,
            BigDecimal confidenceScore,
            String matchMethod) {
        
        String matchType = transactionId != null ? "TRANSACTION" : "CHECK";
        
        return ReconciliationMatch.builder()
            .id(UUID.randomUUID())
            .statementLineId(statementLineId)
            .bankTransactionId(transactionId)
            .checkId(checkId)
            .matchType(matchType)
            .matchMethod(matchMethod)
            .matchedAmount(matchedAmount)
            .confidenceScore(confidenceScore)
            .matchedBy("SYSTEM")
            .createdAt(LocalDateTime.now())
            .isNew(true)
            .build();
    }

    /**
     * Converts a ReconciliationMatch entity to a response DTO.
     *
     * @param entity the entity
     * @return the response DTO
     */
    public ReconciliationMatchResponse toResponse(ReconciliationMatch entity) {
        return ReconciliationMatchResponse.builder()
            .id(entity.getId())
            .statementLineId(entity.getStatementLineId())
            .bankTransactionId(entity.getBankTransactionId())
            .checkId(entity.getCheckId())
            .matchType(entity.getMatchType())
            .matchMethod(entity.getMatchMethod())
            .matchedAmount(entity.getMatchedAmount())
            .confidenceScore(entity.getConfidenceScore())
            .notes(entity.getNotes())
            .matchedBy(entity.getMatchedBy())
            .createdAt(entity.getCreatedAt())
            .build();
    }

    /**
     * Converts a ReconciliationMatch entity to a response DTO with additional details.
     *
     * @param entity the entity
     * @param transactionReference the bank transaction reference
     * @param checkNumber the check number
     * @return the response DTO
     */
    public ReconciliationMatchResponse toResponseWithDetails(
            ReconciliationMatch entity,
            String transactionReference,
            String checkNumber) {
        ReconciliationMatchResponse response = toResponse(entity);
        response.setBankTransactionReference(transactionReference);
        response.setCheckNumber(checkNumber);
        return response;
    }

    /**
     * Determines the match type based on the request.
     *
     * @param request the reconciliation request
     * @return the match type
     */
    private String determineMatchType(ReconcileManualRequest request) {
        boolean hasTransaction = request.getBankTransactionId() != null;
        boolean hasCheck = request.getCheckId() != null;
        
        if (hasTransaction && hasCheck) {
            return "MULTIPLE";
        } else if (hasTransaction) {
            return "TRANSACTION";
        } else if (hasCheck) {
            return "CHECK";
        }
        return "PARTIAL";
    }
}
