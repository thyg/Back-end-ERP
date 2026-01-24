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

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a checkbook (chéquier).
 *
 * <p>This entity maps to the treasury.checkbooks table and manages
 * ranges of check numbers associated with a bank account.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "treasury", name = "checkbooks")
public class Checkbook implements Persistable<UUID> {

    /**
     * Unique identifier for the checkbook.
     */
    @Id
    private UUID id;

    /**
     * Reference to the bank account this checkbook belongs to.
     */
    @Column("bank_account_id")
    private UUID bankAccountId;

    /**
     * IBAN associated with this checkbook.
     */
    @Column("iban")
    private String iban;

    /**
     * Common prefix/root for check numbers (e.g., "4587").
     */
    @Column("prefix")
    private String prefix;

    /**
     * First check number in the range (e.g., 458701).
     */
    @Column("start_number")
    private Integer startNumber;

    /**
     * Last check number in the range (e.g., 458750).
     */
    @Column("end_number")
    private Integer endNumber;

    /**
     * Number of pages (checks) in the checkbook.
     */
    @Column("number_of_pages")
    private Integer numberOfPages;

    /**
     * Next available check number to use.
     */
    @Column("current_number")
    private Integer currentNumber;

    /**
     * Status of the checkbook: ACTIVE, FINISHED, or CANCELLED.
     */
    @Column("status")
    private String status;

    /**
     * Type of checkbook: REEL (physical) or FICTIF (system/virtual).
     */
    @Column("type")
    @Builder.Default
    private String type = "REEL";

    /**
     * Indicates if this is a system checkbook (cannot be modified/deleted).
     */
    @Column("is_system")
    @Builder.Default
    private Boolean isSystem = false;

    /**
     * Next sequence number for fictif checkbook.
     */
    @Column("next_sequence")
    private Long nextSequence;

    /**
     * Timestamp when the record was created.
     */
    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    /**
     * Timestamp when the record was last updated.
     */
    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

    /**
     * Transient flag to indicate if this is a new entity.
     */
    @Transient
    @Builder.Default
    private boolean isNew = true;

    /**
     * Gets the next check number formatted with the prefix.
     *
     * @return the formatted check number (e.g., "4587000001")
     */
    public String getNextCheckNumber() {
        if (currentNumber == null || prefix == null) {
            return null;
        }
        return prefix + String.format("%06d", currentNumber);
    }

    /**
     * Checks if the checkbook has available checks.
     * Fictif checkbooks always have unlimited checks.
     *
     * @return true if checks are available and status is ACTIVE
     */
    public boolean hasAvailableChecks() {
        if (isFictif()) {
            return "ACTIVE".equals(status);
        }
        return currentNumber != null
            && endNumber != null
            && currentNumber <= endNumber
            && "ACTIVE".equals(status);
    }

    /**
     * Calculates the number of remaining checks.
     * Returns -1 for fictif checkbooks (unlimited).
     *
     * @return the number of available checks, -1 for unlimited (fictif), or 0 if none
     */
    public int getAvailableChecksCount() {
        if (isFictif()) {
            return -1; // Unlimited
        }
        if (currentNumber == null || endNumber == null || currentNumber > endNumber) {
            return 0;
        }
        return endNumber - currentNumber + 1;
    }

    @Override
    public UUID getId() {
        return this.id;
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    /**
     * Marks this entity as not new (for updates).
     *
     * @return this entity for method chaining
     */
    public Checkbook markNotNew() {
        this.isNew = false;
        return this;
    }

    /**
     * Checks if this is the fictif system checkbook.
     *
     * @return true if this is a fictif checkbook
     */
    public boolean isFictif() {
        return "FICTIF".equals(type);
    }

    /**
     * Checks if this is a real (physical) checkbook.
     *
     * @return true if this is a real checkbook
     */
    public boolean isReel() {
        return "REEL".equals(type);
    }

    /**
     * Generates the next check number for received checks (fictif checkbook).
     * Format: CHQ-REC-000001, CHQ-REC-000002, etc.
     *
     * @return formatted check number for received checks
     */
    public String getNextReceivedCheckNumber() {
        if (!isFictif() || nextSequence == null) {
            return null;
        }
        return "CHQ-REC-" + String.format("%06d", nextSequence);
    }
}
