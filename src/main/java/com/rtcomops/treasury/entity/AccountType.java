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
 * Entity representing an account type configuration.
 *
 * <p>This entity maps to the treasury.account_types table and defines
 * the different types of bank accounts (e.g., CHEQUE, ESPECES, EPARGNE).</p>
 *
 * <p>Each account type has configurable permissions for checks and cash
 * transactions, as well as overdraft settings.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "treasury", name = "account_types")
public class AccountType implements Persistable<UUID> {

    /**
     * Unique identifier for the account type.
     */
    @Id
    private UUID id;

    /**
     * Unique code identifying the account type (e.g., "CHEQUE", "ESPECES").
     */
    @Column("code")
    private String code;

    /**
     * Human-readable label for the account type.
     */
    @Column("libelle")
    private String libelle;

    /**
     * Optional description of the account type.
     */
    @Column("description")
    private String description;

    /**
     * Indicates if accounts of this type can issue checks.
     */
    @Column("peut_emettre_cheques")
    private Boolean peutEmettreChecques;

    /**
     * Indicates if accounts of this type can receive checks.
     */
    @Column("peut_recevoir_cheques")
    private Boolean peutRecevoirChecques;

    /**
     * Indicates if accounts of this type can have cash transactions.
     */
    @Column("peut_transactions_especes")
    private Boolean peutTransactionsEspeces;

    /**
     * Indicates if overdraft is authorized for this account type.
     */
    @Column("decouvert_autorise")
    private Boolean decouvertAutorise;

    /**
     * Default overdraft limit for accounts of this type.
     */
    @Column("decouvert_par_defaut")
    private BigDecimal decouvertParDefaut;

    /**
     * Indicates if this account type is active and available for use.
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
    public AccountType markNotNew() {
        this.isNew = false;
        return this;
    }
}
