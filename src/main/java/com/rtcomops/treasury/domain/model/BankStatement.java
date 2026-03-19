package com.rtcomops.treasury.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model representing an imported bank statement.
 *
 * <p>This is a pure POJO without any framework dependencies.
 * It represents the core business concept of a Bank Statement
 * used for bank reconciliation.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class BankStatement {

    /**
     * Unique identifier for the bank statement.
     */
    private UUID id;

    /**
     * Reference to the bank account this statement belongs to.
     */
    private UUID bankAccountId;

    /**
     * Unique reference code for the statement.
     */
    private String reference;

    /**
     * Date of the statement.
     */
    private LocalDate statementDate;

    /**
     * Start date of the statement period.
     */
    private LocalDate periodStart;

    /**
     * End date of the statement period.
     */
    private LocalDate periodEnd;

    /**
     * Opening balance at the start of the period.
     */
    private BigDecimal openingBalance;

    /**
     * Closing balance at the end of the period.
     */
    private BigDecimal closingBalance;

    /**
     * Total credit transactions in the statement.
     */
    private BigDecimal totalCredits;

    /**
     * Total debit transactions in the statement.
     */
    private BigDecimal totalDebits;

    /**
     * Number of transaction lines in the statement.
     */
    private Integer lineCount;

    /**
     * Number of reconciled lines.
     */
    private Integer reconciledCount;

    /**
     * Status of the statement (IMPORTED, IN_PROGRESS, CLOSED).
     */
    private String status;

    /**
     * Source of the import (MANUAL, CSV, OFX, etc.).
     */
    private String importSource;

    /**
     * Original file name if imported.
     */
    private String fileName;

    /**
     * Additional notes about the statement.
     */
    private String notes;

    /**
     * Timestamp when the record was created.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the record was last updated.
     */
    private LocalDateTime updatedAt;

    /**
     * Calculates the reconciliation progress percentage.
     *
     * @return progress percentage (0-100)
     */
    public BigDecimal getReconciliationProgress() {
        if (lineCount == null || lineCount == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(reconciledCount)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(lineCount), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Checks if the statement is fully reconciled.
     *
     * @return true if all lines are reconciled
     */
    public boolean isFullyReconciled() {
        return lineCount != null && reconciledCount != null && lineCount.equals(reconciledCount);
    }

    /**
     * Checks if the statement is closed.
     *
     * @return true if status is CLOSED
     */
    public boolean isClosed() {
        return "CLOSED".equals(status);
    }
}
