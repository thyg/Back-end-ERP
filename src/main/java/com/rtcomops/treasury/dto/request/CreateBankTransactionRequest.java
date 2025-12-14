package com.rtcomops.treasury.dto.request;

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
 * DTO for creating a new bank transaction.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBankTransactionRequest {

    @NotNull(message = "Bank account ID is required")
    private UUID bankAccountId;

    @NotNull(message = "Transaction type ID is required")
    private UUID transactionTypeId;

    @Size(max = 50, message = "Reference must not exceed 50 characters")
    private String reference;

    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;

    private LocalDate valueDate;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Direction is required")
    @Pattern(regexp = "^(CREDIT|DEBIT)$", message = "Direction must be CREDIT or DEBIT")
    private String direction;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Size(max = 100, message = "Partner name must not exceed 100 characters")
    private String partnerName;
}
