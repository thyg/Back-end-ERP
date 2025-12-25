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
 * Entity representing a transaction type category.
 *
 * <p>This entity maps to the treasury.transaction_types table and defines
 * the different types of transactions (e.g., transfer, check, cash).</p>
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
@Table(schema = "treasury", name = "transaction_types")
public class TransactionType implements Persistable<UUID> {

    /**
     * Unique identifier for the transaction type.
     */
    @Id
    private UUID id;

    /**
     * Unique code identifying the transaction type (e.g., "VIR", "CHQ").
     */
    @Column("code")
    private String code;

    /**
     * Human-readable label for the transaction type.
     */
    @Column("label")
    private String label;

    /**
     * Category of the transaction: BANK, CASH, CHECK, or OTHER.
     */
    @Column("category")
    private String category;

    /**
     * Optional description of the transaction type.
     */
    @Column("description")
    private String description;

    /**
     * Indicates if the transaction type is active and available for use.
     */
    @Column("is_active")
    private Boolean isActive;

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
    public TransactionType markNotNew() {
        this.isNew = false;
        return this;
    }
}
