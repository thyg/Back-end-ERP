package com.rtcomops.treasury.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
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
 * DTO for updating an existing bank transaction.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBankTransactionRequest {

    private UUID transactionTypeId;

    @Size(max = 50, message = "Reference must not exceed 50 characters")
    private String reference;

    private LocalDate transactionDate;

    private LocalDate valueDate;

    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Pattern(regexp = "^(CREDIT|DEBIT)$", message = "Direction must be CREDIT or DEBIT")
    private String direction;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Size(max = 100, message = "Partner name must not exceed 100 characters")
    private String partnerName;

    @Pattern(regexp = "^(DRAFT|VALIDATED|CANCELLED)$", message = "Status must be DRAFT, VALIDATED, or CANCELLED")
    private String status;
}
