package com.rtcomops.treasury.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model representing a reconciliation match between a statement line and internal records.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationMatch {

    private UUID id;
    private UUID statementLineId;
    private UUID bankTransactionId;
    private UUID checkId;
    private String matchType;
    private String matchMethod;
    private BigDecimal matchedAmount;
    private BigDecimal confidenceScore;
    private String notes;
    private String matchedBy;
    private LocalDateTime createdAt;
}
