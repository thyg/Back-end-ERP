package com.rtcomops.treasury.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model representing a banking institution.
 *
 * <p>This is a pure POJO without any framework dependencies.
 * It represents the core business concept of a Bank.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bank {

    /**
     * Unique identifier for the bank.
     */
    private UUID id;

    /**
     * Unique code identifying the bank (e.g., "BNP", "SG").
     */
    private String code;

    /**
     * Full name of the bank.
     */
    private String name;

    /**
     * SWIFT/BIC code for international transfers.
     */
    private String swiftCode;

    /**
     * National bank code (5 digits) used for IBAN generation.
     * This is different from SWIFT code.
     */
    private String bankCode;

    /**
     * Country where the bank is headquartered.
     */
    private String country;

    /**
     * Full address of the bank (headquarters or branch).
     */
    private String address;

    /**
     * Indicates if the bank is active and available for selection.
     */
    private Boolean isActive;

    /**
     * Reference to the bank category.
     */
    private UUID bankCategoryId;

    /**
     * Timestamp when the record was created.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the record was last updated.
     */
    private LocalDateTime updatedAt;
}
