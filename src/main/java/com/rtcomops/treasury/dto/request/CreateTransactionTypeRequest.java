package com.rtcomops.treasury.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new transaction type.
 *
 * <p>Contains validation rules to ensure data integrity
 * when creating a transaction type record.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionTypeRequest {

    /**
     * Unique code for the transaction type (2-20 characters).
     */
    @NotBlank(message = "Transaction type code is required")
    @Size(min = 2, max = 20, message = "Code must be between 2 and 20 characters")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Code must contain only letters, numbers, and underscores")
    private String code;

    /**
     * Human-readable label (2-100 characters).
     */
    @NotBlank(message = "Label is required")
    @Size(min = 2, max = 100, message = "Label must be between 2 and 100 characters")
    private String label;

    /**
     * Category of the transaction: BANK, CASH, CHECK, or OTHER.
     */
    @NotBlank(message = "Category is required")
    @Pattern(regexp = "^(BANK|CASH|CHECK|OTHER)$", message = "Category must be BANK, CASH, CHECK, or OTHER")
    private String category;

    /**
     * Optional description of the transaction type.
     */
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    /**
     * Whether the transaction type is active. Defaults to true if not specified.
     */
    private Boolean isActive;
}
