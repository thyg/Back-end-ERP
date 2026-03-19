package com.rtcomops.treasury.application.mapper;

import com.rtcomops.treasury.application.dto.request.ReconcileManualRequest;
import com.rtcomops.treasury.application.dto.response.ReconciliationMatchResponse;
import com.rtcomops.treasury.domain.model.ReconciliationMatch;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper for ReconciliationMatch domain model to DTO conversion.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Component
public class ReconciliationMatchMapper {

    /**
     * Converts a domain model to a response DTO.
     *
     * @param match the domain model
     * @return the response DTO
     */
    public ReconciliationMatchResponse toResponse(ReconciliationMatch match) {
        if (match == null) {
            return null;
        }
        return ReconciliationMatchResponse.builder()
                .id(match.getId())
                .statementLineId(match.getStatementLineId())
                .bankTransactionId(match.getBankTransactionId())
                .checkId(match.getCheckId())
                .matchType(match.getMatchType())
                .matchMethod(match.getMatchMethod())
                .matchedAmount(match.getMatchedAmount())
                .confidenceScore(match.getConfidenceScore())
                .notes(match.getNotes())
                .matchedBy(match.getMatchedBy())
                .createdAt(match.getCreatedAt())
                .build();
    }

    /**
     * Converts a response DTO to a domain model.
     *
     * @param response the response DTO
     * @return the domain model
     */
    public ReconciliationMatch toDomain(ReconciliationMatchResponse response) {
        if (response == null) {
            return null;
        }
        return ReconciliationMatch.builder()
                .id(response.getId())
                .statementLineId(response.getStatementLineId())
                .bankTransactionId(response.getBankTransactionId())
                .checkId(response.getCheckId())
                .matchType(response.getMatchType())
                .matchMethod(response.getMatchMethod())
                .matchedAmount(response.getMatchedAmount())
                .confidenceScore(response.getConfidenceScore())
                .notes(response.getNotes())
                .matchedBy(response.getMatchedBy())
                .createdAt(response.getCreatedAt())
                .build();
    }

    /**
     * Creates a ReconciliationMatch entity from manual reconciliation request.
     *
     * @param request the manual reconciliation request
     * @param matchedAmount the amount matched
     * @return the domain model
     */
    public ReconciliationMatch toEntityFromManual(ReconcileManualRequest request, BigDecimal matchedAmount) {
        return ReconciliationMatch.builder()
                .id(UUID.randomUUID())
                .statementLineId(request.getStatementLineId())
                .bankTransactionId(request.getBankTransactionId())
                .checkId(request.getCheckId())
                .matchType(request.getBankTransactionId() != null ? "TRANSACTION" : "CHECK")
                .matchMethod("MANUAL")
                .matchedAmount(matchedAmount)
                .confidenceScore(BigDecimal.valueOf(100))
                .notes(request.getNotes())
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a ReconciliationMatch entity for automatic reconciliation.
     *
     * @param statementLineId the statement line ID
     * @param bankTransactionId the bank transaction ID (can be null)
     * @param checkId the check ID (can be null)
     * @param matchedAmount the amount matched
     * @param confidenceScore the confidence score
     * @param matchMethod the match method (AUTO_EXACT, AUTO_FUZZY, etc.)
     * @return the domain model
     */
    public ReconciliationMatch toEntityFromAuto(
            UUID statementLineId,
            UUID bankTransactionId,
            UUID checkId,
            BigDecimal matchedAmount,
            BigDecimal confidenceScore,
            String matchMethod) {
        return ReconciliationMatch.builder()
                .id(UUID.randomUUID())
                .statementLineId(statementLineId)
                .bankTransactionId(bankTransactionId)
                .checkId(checkId)
                .matchType(bankTransactionId != null ? "TRANSACTION" : "CHECK")
                .matchMethod(matchMethod)
                .matchedAmount(matchedAmount)
                .confidenceScore(confidenceScore)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Converts a ReconciliationMatch with additional details to a response DTO.
     *
     * @param match the domain model
     * @param transactionReference the transaction reference
     * @param checkNumber the check number
     * @return the response DTO with details
     */
    public ReconciliationMatchResponse toResponseWithDetails(
            ReconciliationMatch match,
            String transactionReference,
            String checkNumber) {
        ReconciliationMatchResponse response = toResponse(match);
        response.setBankTransactionReference(transactionReference);
        response.setCheckNumber(checkNumber);
        return response;
    }
}
