package com.rtcomops.treasury.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a batch check deposit (remise de cheques en lot).
 *
 * <p>This entity maps to the treasury.check_deposits table and contains
 * information about grouped check deposits for easier bank reconciliation.</p>
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
 * @version 1.1.0
 * @since 2026-02-16
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "treasury", name = "check_deposits")
public class CheckDeposit implements Persistable<UUID> {

    @Id
    private UUID id;

    /**
     * Unique reference for the deposit batch (e.g., REM-202602-0001).
     */
    @Column("reference")
    private String reference;

    /**
     * Date when the checks were deposited at the bank.
     */
    @Column("deposit_date")
    private LocalDate depositDate;

    /**
     * Bank account receiving the deposit.
     */
    @Column("bank_account_id")
    private UUID bankAccountId;

    /**
     * Sum of all check amounts in the batch.
     */
    @Column("total_amount")
    private BigDecimal totalAmount;

    /**
     * Number of checks in the batch.
     */
    @Column("check_count")
    private Integer checkCount;

    /**
     * Current status of the deposit.
     * PENDING = created, awaiting bank deposit; DEPOSITED = at bank, awaiting cash;
     * CASHED = funds received; RECONCILED = matched with bank statement.
     */
    @Column("status")
    private String status;

    /**
     * Date when the deposit was cashed (funds received in account).
     * Set when transitioning from DEPOSITED to CASHED.
     */
    @Column("cashed_date")
    private LocalDate cashedDate;

    /**
     * Link to the unique bank transaction created upon cashing.
     */
    @Column("bank_transaction_id")
    private UUID bankTransactionId;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return this.id;
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    public CheckDeposit markNotNew() {
        this.isNew = false;
        return this;
    }
}
