package com.rtcomops.treasury.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for reconciliation summary response.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationSummaryResponse {

    private UUID bankStatementId;
    private String bankAccountName;
    private Integer totalLines;
    private Integer matchedLines;
    private Integer unmatchedLines;
    private Integer ignoredLines;
    private BigDecimal progressPercentage;
    private BigDecimal totalCredits;
    private BigDecimal totalDebits;
    private BigDecimal matchedCredits;
    private BigDecimal matchedDebits;
    private BigDecimal unmatchedCredits;
    private BigDecimal unmatchedDebits;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private BigDecimal calculatedBalance;
    private BigDecimal balanceDifference;
    private String status;
}
