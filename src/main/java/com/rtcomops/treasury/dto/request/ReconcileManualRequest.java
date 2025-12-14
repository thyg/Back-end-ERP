package com.rtcomops.treasury.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for manual reconciliation request.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconcileManualRequest {

    @NotNull(message = "Statement line ID is required")
    private UUID statementLineId;

    private UUID bankTransactionId;

    private UUID checkId;

    private BigDecimal matchedAmount;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    @Size(max = 100, message = "Matched by must not exceed 100 characters")
    private String matchedBy;
}
