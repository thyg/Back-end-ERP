package com.rtcomops.treasury.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for updating an existing check.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCheckRequest {

    @Size(min = 1, max = 20, message = "Check number must be between 1 and 20 characters")
    private String checkNumber;

    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Size(min = 2, max = 100, message = "Partner name must be between 2 and 100 characters")
    private String partnerName;

    private LocalDate issueDate;

    private LocalDate dueDate;

    private LocalDate depositDate;

    private LocalDate cashedDate;

    private String status;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Size(max = 255, message = "Rejection reason must not exceed 255 characters")
    private String rejectionReason;

    @Size(max = 100, message = "Issuer bank must not exceed 100 characters")
    private String issuerBank;
}
