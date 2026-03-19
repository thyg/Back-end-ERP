package com.rtcomops.treasury.infrastructure.persistence.entity;

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
 * Persistence entity for AccountType.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "treasury", name = "account_types")
public class AccountTypeEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("code")
    private String code;

    @Column("libelle")
    private String libelle;

    @Column("description")
    private String description;

    @Column("peut_emettre_cheques")
    private Boolean peutEmettreChecques;

    @Column("peut_recevoir_cheques")
    private Boolean peutRecevoirChecques;

    @Column("peut_transactions_especes")
    private Boolean peutTransactionsEspeces;

    @Column("decouvert_autorise")
    private Boolean decouvertAutorise;

    @Column("decouvert_par_defaut")
    private BigDecimal decouvertParDefaut;

    @Column("is_active")
    private Boolean isActive;

    @Column("ordre_affichage")
    private Integer ordreAffichage;

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

    public AccountTypeEntity markNotNew() {
        this.isNew = false;
        return this;
    }
}
