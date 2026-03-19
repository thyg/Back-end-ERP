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
 * Domain model representing a batch check deposit (remise de cheques en lot).
 *
 * <p>Workflow Statuses:</p>
 * <ul>
 *   <li>PENDING - Initial state: deposit created, checks assigned, awaiting bank deposit confirmation</li>
 *   <li>DEPOSITED - Checks physically deposited at the bank, awaiting cash receipt</li>
 *   <li>CASHED - Funds received in account, bank transaction created</li>
 *   <li>RECONCILED - Matched with a bank statement line</li>
 * </ul>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-13
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CheckDeposit {

    private UUID id;

    /**
     * Unique reference for the deposit batch (e.g., REM-202602-0001).
     */
    private String reference;

    /**
     * Date when the checks were deposited at the bank.
     */
    private LocalDate depositDate;

    /**
     * Bank account receiving the deposit.
     */
    private UUID bankAccountId;

    /**
     * Sum of all check amounts in the batch.
     */
    private BigDecimal totalAmount;

    /**
     * Number of checks in the batch.
     */
    private Integer checkCount;

    /**
     * Current status of the deposit.
     * PENDING = created, awaiting bank deposit; DEPOSITED = at bank, awaiting cash;
     * CASHED = funds received; RECONCILED = matched with bank statement.
     */
    private String status;

    /**
     * Date when the deposit was cashed (funds received in account).
     * Set when transitioning from DEPOSITED to CASHED.
     */
    private LocalDate cashedDate;

    /**
     * Link to the unique bank transaction created upon cashing.
     */
    private UUID bankTransactionId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
