package com.rtcomops.treasury.application.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for updating an existing bank statement.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBankStatementRequest {

    @Size(max = 50, message = "Reference must not exceed 50 characters")
    private String reference;

    private LocalDate statementDate;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private BigDecimal openingBalance;

    private BigDecimal closingBalance;

    @Pattern(regexp = "^(IMPORTED|IN_PROGRESS|RECONCILED|CLOSED)$",
             message = "Status must be IMPORTED, IN_PROGRESS, RECONCILED, or CLOSED")
    private String status;

    private String notes;
}
