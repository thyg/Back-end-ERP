package com.rtcomops.treasury.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for creating a new statement line.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStatementLineRequest {

    @NotNull(message = "Bank statement ID is required")
    private UUID bankStatementId;

    private Integer lineNumber;

    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;

    private LocalDate valueDate;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Direction is required")
    @Pattern(regexp = "^(CREDIT|DEBIT)$", message = "Direction must be CREDIT or DEBIT")
    private String direction;

    @Size(max = 100, message = "Reference must not exceed 100 characters")
    private String reference;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Size(max = 100, message = "Partner name must not exceed 100 characters")
    private String partnerName;

    @Size(max = 50, message = "Partner account must not exceed 50 characters")
    private String partnerAccount;

    private BigDecimal balanceAfter;

    private String rawData;
}
