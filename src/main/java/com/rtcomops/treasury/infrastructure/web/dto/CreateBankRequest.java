package com.rtcomops.treasury.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for creating a new bank.
 *
 * <p>Contains validation rules to ensure data integrity
 * when creating a bank record.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBankRequest {

    /**
     * Unique code for the bank (2-20 characters, alphanumeric with underscore).
     */
    @NotBlank(message = "Bank code is required")
    @Size(min = 2, max = 20, message = "Bank code must be between 2 and 20 characters")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Bank code must contain only letters, numbers, and underscores")
    private String code;

    /**
     * Full name of the bank (2-100 characters).
     */
    @NotBlank(message = "Bank name is required")
    @Size(min = 2, max = 100, message = "Bank name must be between 2 and 100 characters")
    private String name;

    /**
     * SWIFT/BIC code (optional, max 11 characters).
     */
    @Size(max = 11, message = "SWIFT code must not exceed 11 characters")
    @Pattern(regexp = "^[A-Za-z0-9]*$", message = "SWIFT code must contain only letters and numbers")
    private String swiftCode;

    /**
     * National bank code (5 digits) for IBAN generation.
     */
    @Size(max = 5, message = "Bank code must not exceed 5 characters")
    @Pattern(regexp = "^[0-9]*$", message = "Bank code must contain only digits")
    private String bankCode;

    /**
     * Country where the bank is located.
     */
    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;

    /**
     * Full address of the bank (optional, max 500 characters).
     */
    @Size(max = 500, message = "L'adresse ne doit pas dépasser 500 caractères")
    private String address;

    /**
     * Whether the bank is active. Defaults to true if not specified.
     */
    private Boolean isActive;

    /**
     * The category of the bank (e.g., BANK, MOBILE_MONEY, MICROFINANCE).
     */
    private UUID bankCategoryId;
}
