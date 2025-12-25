package com.rtcomops.treasury.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * SWIFT/BIC code (8 or 11 characters).
     */
    @Size(max = 11, message = "SWIFT code must not exceed 11 characters")
    @Pattern(regexp = "^$|^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", 
             message = "Invalid SWIFT code format")
    private String swiftCode;

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
}
