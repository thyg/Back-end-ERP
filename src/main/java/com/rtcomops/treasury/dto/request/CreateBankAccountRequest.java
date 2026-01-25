package com.rtcomops.treasury.dto.request;

import com.rtcomops.treasury.validation.ValidIban;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map; // Import
import java.util.UUID; // Import

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for creating a new bank account.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBankAccountRequest {

    @NotNull(message = "Bank ID is required")
    private UUID bankId;

    private UUID accountTypeId;

    private UUID accountSubTypeId;

    private UUID connectorTypeId;

    private UUID journalId;

    @NotBlank(message = "Account name is required")
    @Size(min = 2, max = 100)
    private String name;

    /**
     * Branch/agency code (5 digits) for IBAN generation.
     */
    @Size(max = 5, message = "Branch code must not exceed 5 characters")
    @Pattern(regexp = "^[0-9]*$", message = "Branch code must contain only digits")
    private String branchCode;

    /**
     * Account number (11 digits for Cameroon).
     */
    private String accountNumber;

    /**
     * Generated IBAN (computed from bank code, branch code, and account number).
     */
    private String generatedIban;

    private String currency;
    private BigDecimal initialBalance;
    private Boolean isActive;

    // Gestion du découvert
    private Boolean overdraftAllowed;
    private BigDecimal overdraftLimit;

    // Champ pour les données dynamiques (numéro de tél, etc.)
    private Map<String, Object> details;
}
