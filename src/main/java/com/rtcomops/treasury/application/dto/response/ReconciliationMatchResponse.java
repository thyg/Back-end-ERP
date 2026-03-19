package com.rtcomops.treasury.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for reconciliation match response data.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationMatchResponse {

    private UUID id;
    private UUID statementLineId;
    private UUID bankTransactionId;
    private String bankTransactionReference;
    private UUID checkId;
    private String checkNumber;
    private String matchType;
    private String matchMethod;
    private BigDecimal matchedAmount;
    private BigDecimal confidenceScore;
    private String notes;
    private String matchedBy;
    private LocalDateTime createdAt;
}
