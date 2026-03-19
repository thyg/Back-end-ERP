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
 * Domain model representing a single line from a bank statement.
 *
 * <p>This is a pure POJO without any framework dependencies.
 * It represents individual transaction entries from bank statements
 * used for reconciliation.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class StatementLine {

    /**
     * Unique identifier for the statement line.
     */
    private UUID id;

    /**
     * Reference to the parent bank statement.
     */
    private UUID bankStatementId;

    /**
     * Sequential line number within the statement.
     */
    private Integer lineNumber;

    /**
     * Date when the transaction occurred.
     */
    private LocalDate transactionDate;

    /**
     * Value date of the transaction.
     */
    private LocalDate valueDate;

    /**
     * Transaction amount (always positive).
     */
    private BigDecimal amount;

    /**
     * Direction of the transaction (CREDIT or DEBIT).
     */
    private String direction;

    /**
     * Transaction reference code.
     */
    private String reference;

    /**
     * Description of the transaction.
     */
    private String description;

    /**
     * Name of the transaction partner.
     */
    private String partnerName;

    /**
     * Account number of the transaction partner.
     */
    private String partnerAccount;

    /**
     * Account balance after this transaction.
     */
    private BigDecimal balanceAfter;

    /**
     * Reconciliation status (UNMATCHED, MATCHED, IGNORED).
     */
    private String reconciliationStatus;

    /**
     * Timestamp when the line was reconciled.
     */
    private LocalDateTime reconciledAt;

    /**
     * Raw data from import (JSON or original format).
     */
    private String rawData;

    /**
     * Timestamp when the record was created.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the record was last updated.
     */
    private LocalDateTime updatedAt;

    /**
     * Checks if the line is a credit transaction.
     *
     * @return true if direction is CREDIT
     */
    public boolean isCredit() {
        return "CREDIT".equalsIgnoreCase(direction);
    }

    /**
     * Checks if the line is a debit transaction.
     *
     * @return true if direction is DEBIT
     */
    public boolean isDebit() {
        return "DEBIT".equalsIgnoreCase(direction);
    }

    /**
     * Checks if the line is matched.
     *
     * @return true if reconciliation status is MATCHED
     */
    public boolean isMatched() {
        return "MATCHED".equalsIgnoreCase(reconciliationStatus);
    }

    /**
     * Checks if the line is unmatched.
     *
     * @return true if reconciliation status is UNMATCHED
     */
    public boolean isUnmatched() {
        return "UNMATCHED".equalsIgnoreCase(reconciliationStatus);
    }

    /**
     * Checks if the line is ignored.
     *
     * @return true if reconciliation status is IGNORED
     */
    public boolean isIgnored() {
        return "IGNORED".equalsIgnoreCase(reconciliationStatus);
    }

    /**
     * Marks this line as matched.
     */
    public void markAsMatched() {
        this.reconciliationStatus = "MATCHED";
        this.reconciledAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marks this line as unmatched.
     */
    public void markAsUnmatched() {
        this.reconciliationStatus = "UNMATCHED";
        this.reconciledAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marks this line as ignored.
     */
    public void markAsIgnored() {
        this.reconciliationStatus = "IGNORED";
        this.updatedAt = LocalDateTime.now();
    }
}
