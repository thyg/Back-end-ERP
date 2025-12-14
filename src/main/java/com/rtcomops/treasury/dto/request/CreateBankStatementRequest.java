package com.rtcomops.treasury.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for creating a new bank statement.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBankStatementRequest {

    @NotNull(message = "Bank account ID is required")
    private UUID bankAccountId;

    @Size(max = 50, message = "Reference must not exceed 50 characters")
    private String reference;

    @NotNull(message = "Statement date is required")
    private LocalDate statementDate;

    @NotNull(message = "Period start date is required")
    private LocalDate periodStart;

    @NotNull(message = "Period end date is required")
    private LocalDate periodEnd;

    @NotNull(message = "Opening balance is required")
    private BigDecimal openingBalance;

    @NotNull(message = "Closing balance is required")
    private BigDecimal closingBalance;

    @Size(max = 50, message = "Import source must not exceed 50 characters")
    private String importSource;

    @Size(max = 255, message = "File name must not exceed 255 characters")
    private String fileName;

    private String notes;
}
