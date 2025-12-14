package com.rtcomops.treasury.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @Pattern(regexp = "^$|^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", 
             message = "Invalid SWIFT code format")
    private String swiftCode;

    /**
     * Updated country (optional).
     */
    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;

    /**
     * Updated active status (optional).
     */
    private Boolean isActive;
}
