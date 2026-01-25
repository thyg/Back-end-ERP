package com.rtcomops.treasury.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for updating an existing bank.
 *
 * <p>All fields are optional. Only provided fields will be updated.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBankRequest {

    /**
     * Updated bank code (optional).
     */
    @Size(min = 2, max = 20, message = "Bank code must be between 2 and 20 characters")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Bank code must contain only letters, numbers, and underscores")
    private String code;

    /**
     * Updated bank name (optional).
     */
    @Size(min = 2, max = 100, message = "Bank name must be between 2 and 100 characters")
    private String name;

    /**
     * Updated SWIFT code (optional).
     */
    @Size(max = 11, message = "SWIFT code must not exceed 11 characters")
    @Pattern(regexp = "^[A-Za-z0-9]*$", message = "SWIFT code must contain only letters and numbers")
    private String swiftCode;

    /**
     * Updated national bank code (5 digits) for IBAN generation.
     */
    @Size(max = 5, message = "Bank code must not exceed 5 characters")
    @Pattern(regexp = "^[0-9]*$", message = "Bank code must contain only digits")
    private String bankCode;

    /**
     * Updated country (optional).
     */
    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;

    /**
     * Updated address (optional, max 500 characters).
     */
    @Size(max = 500, message = "L'adresse ne doit pas dépasser 500 caractères")
    private String address;

    /**
     * Updated active status (optional).
     */
    private Boolean isActive;

    /**
     * Updated bank category (optional).
     */
    private UUID bankCategoryId;
}
