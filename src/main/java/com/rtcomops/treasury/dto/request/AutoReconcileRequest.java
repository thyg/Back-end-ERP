package com.rtcomops.treasury.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for automatic reconciliation request.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoReconcileRequest {

    @NotNull(message = "Bank statement ID is required")
    private UUID bankStatementId;

    @Builder.Default
    private Boolean matchByAmount = true;

    @Builder.Default
    private Boolean matchByReference = true;

    @Builder.Default
    private Boolean matchByDate = true;

    @Builder.Default
    private Integer dateToleranceDays = 3;

    @Builder.Default
    private BigDecimal amountTolerance = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal minimumConfidenceScore = new BigDecimal("80.00");
}
