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
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an account sub-type configuration.
 *
 * <p>This entity maps to the treasury.account_sub_types table and defines
 * sub-categories of account types with optional parameter overrides.</p>
 *
 * <p>Override fields that are NULL inherit their value from the parent
 * AccountType. Non-null values override the parent's configuration.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "treasury", name = "account_sub_types")
public class AccountSubType implements Persistable<UUID> {

    /**
     * Unique identifier for the account sub-type.
     */
    @Id
    private UUID id;

    /**
     * Reference to the parent account type.
     */
    @Column("account_type_id")
    private UUID accountTypeId;

    /**
     * Unique code identifying the sub-type within its parent type.
     */
    @Column("code")
    private String code;

    /**
     * Human-readable label for the sub-type.
     */
    @Column("libelle")
    private String libelle;

    /**
     * Optional description of the sub-type.
     */
    @Column("description")
    private String description;

    /**
     * Override for check emission permission.
     * NULL means inherit from parent AccountType.
     */
    @Column("peut_emettre_cheques_override")
    private Boolean peutEmettreChequesOverride;

    /**
     * Override for check reception permission.
     * NULL means inherit from parent AccountType.
     */
    @Column("peut_recevoir_cheques_override")
    private Boolean peutRecevoirChequesOverride;

    /**
     * Override for cash transaction permission.
     * NULL means inherit from parent AccountType.
     */
    @Column("peut_transactions_especes_override")
    private Boolean peutTransactionsEspecesOverride;

    /**
     * Override for overdraft authorization.
     * NULL means inherit from parent AccountType.
     */
    @Column("decouvert_autorise_override")
    private Boolean decouvertAutoriseOverride;

    /**
     * Override for default overdraft limit.
     * NULL means inherit from parent AccountType.
     */
    @Column("decouvert_par_defaut_override")
    private BigDecimal decouvertParDefautOverride;

    /**
     * Indicates if this sub-type is active and available for use.
     */
    @Column("is_active")
    private Boolean isActive;

    /**
     * Display order in lists.
     */
    @Column("ordre_affichage")
    private Integer ordreAffichage;

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
    public AccountSubType markNotNew() {
        this.isNew = false;
        return this;
    }

    /**
     * Gets the effective check emission permission, considering parent override.
     *
     * @param parentValue the parent AccountType's value
     * @return the effective value (override if set, otherwise parent)
     */
    public Boolean getEffectivePeutEmettreChecques(Boolean parentValue) {
        return peutEmettreChequesOverride != null ? peutEmettreChequesOverride : parentValue;
    }

    /**
     * Gets the effective check reception permission, considering parent override.
     *
     * @param parentValue the parent AccountType's value
     * @return the effective value (override if set, otherwise parent)
     */
    public Boolean getEffectivePeutRecevoirChecques(Boolean parentValue) {
        return peutRecevoirChequesOverride != null ? peutRecevoirChequesOverride : parentValue;
    }

    /**
     * Gets the effective cash transaction permission, considering parent override.
     *
     * @param parentValue the parent AccountType's value
     * @return the effective value (override if set, otherwise parent)
     */
    public Boolean getEffectivePeutTransactionsEspeces(Boolean parentValue) {
        return peutTransactionsEspecesOverride != null ? peutTransactionsEspecesOverride : parentValue;
    }

    /**
     * Gets the effective overdraft authorization, considering parent override.
     *
     * @param parentValue the parent AccountType's value
     * @return the effective value (override if set, otherwise parent)
     */
    public Boolean getEffectiveDecouvertAutorise(Boolean parentValue) {
        return decouvertAutoriseOverride != null ? decouvertAutoriseOverride : parentValue;
    }

    /**
     * Gets the effective default overdraft limit, considering parent override.
     *
     * @param parentValue the parent AccountType's value
     * @return the effective value (override if set, otherwise parent)
     */
    public BigDecimal getEffectiveDecouvertParDefaut(BigDecimal parentValue) {
        return decouvertParDefautOverride != null ? decouvertParDefautOverride : parentValue;
    }
}
