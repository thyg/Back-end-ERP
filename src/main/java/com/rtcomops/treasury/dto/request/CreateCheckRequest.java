package com.rtcomops.treasury.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
 * DTO for creating a new check.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCheckRequest {

    @NotNull(message = "Bank account ID is required")
    private UUID bankAccountId;

    /**
     * Optional checkbook ID. If provided, the check number will be auto-generated
     * from the checkbook's next available number.
     */
    private UUID checkbookId;

    @NotBlank(message = "Check type is required")
    @Pattern(regexp = "^(ISSUED|RECEIVED)$", message = "Check type must be ISSUED or RECEIVED")
    private String checkType;

    /**
     * Check number. Required if checkbookId is not provided.
     * If checkbookId is provided, this field is ignored and auto-generated.
     */
    @Size(min = 1, max = 20, message = "Check number must be between 1 and 20 characters")
    private String checkNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Partner name is required")
    @Size(min = 2, max = 100, message = "Partner name must be between 2 and 100 characters")
    private String partnerName;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    private LocalDate dueDate;

    private LocalDate receiptDate;

    @Size(max = 100, message = "Issuer bank must not exceed 100 characters")
    private String issuerBank;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
}
