package com.rtcomops.treasury.application.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing transaction type.
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
public class UpdateTransactionTypeRequest {

    /**
     * Updated code (optional).
     */
    @Size(min = 2, max = 20, message = "Code must be between 2 and 20 characters")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Code must contain only letters, numbers, and underscores")
    private String code;

    /**
     * Updated label (optional).
     */
    @Size(min = 2, max = 100, message = "Label must be between 2 and 100 characters")
    private String label;

    /**
     * Updated category (optional).
     */
    @Pattern(regexp = "^(BANK|CASH|CHECK|OTHER)$", message = "Category must be BANK, CASH, CHECK, or OTHER")
    private String category;

    /**
     * Updated description (optional).
     */
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    /**
     * Updated active status (optional).
     */
    private Boolean isActive;
}
