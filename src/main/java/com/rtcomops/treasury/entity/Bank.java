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
 * Entity representing a banking institution.
 *
 * <p>This entity maps to the treasury.banks table and contains
 * reference data for banks used throughout the treasury module.</p>
 *
 * <p>Implements Persistable to properly handle insert vs update with UUID primary keys.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "treasury", name = "banks")
public class Bank implements Persistable<UUID> {

    /**
     * Unique identifier for the bank.
     */
    @Id
    private UUID id;

    /**
     * Unique code identifying the bank (e.g., "BNP", "SG").
     */
    @Column("code")
    private String code;

    /**
     * Full name of the bank.
     */
    @Column("name")
    private String name;

    /**
     * SWIFT/BIC code for international transfers.
     */
    @Column("swift_code")
    private String swiftCode;

    /**
     * National bank code (5 digits) used for IBAN generation.
     * This is different from SWIFT code.
     */
    @Column("bank_code")
    private String bankCode;

    /**
     * Country where the bank is headquartered.
     */
    @Column("country")
    private String country;

    /**
     * Full address of the bank (headquarters or branch).
     */
    @Column("address")
    private String address;

    /**
     * Indicates if the bank is active and available for selection.
     */
    @Column("is_active")
    private Boolean isActive;

   @Column("bank_category_id")
    private UUID bankCategoryId;
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
     * Not persisted to database.
     */
    @Transient
    @Builder.Default
    private boolean isNew = true;

    /**
     * Returns the ID of the entity.
     *
     * @return the UUID identifier
     */
    @Override
    public UUID getId() {
        return this.id;
    }

    /**
     * Indicates whether this entity is new (for insert) or existing (for update).
     *
     * @return true if new entity (insert), false if existing (update)
     */
    @Override
    public boolean isNew() {
        return this.isNew;
    }

    /**
     * Marks this entity as not new (for updates).
     *
     * @return this entity for method chaining
     */
    public Bank markNotNew() {
        this.isNew = false;
        return this;
    }
}
