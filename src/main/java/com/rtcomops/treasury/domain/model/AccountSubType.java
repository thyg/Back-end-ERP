package com.rtcomops.treasury.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model representing an account sub-type configuration.
 *
 * <p>This is a pure POJO without any framework annotations.
 * It defines sub-categories of account types with optional parameter overrides.</p>
 *
 * <p>Override fields that are NULL inherit their value from the parent
 * AccountType. Non-null values override the parent's configuration.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
public class AccountSubType {

    private UUID id;
    private UUID accountTypeId;
    private String code;
    private String libelle;
    private String description;
    private Boolean peutEmettreChequesOverride;
    private Boolean peutRecevoirChequesOverride;
    private Boolean peutTransactionsEspecesOverride;
    private Boolean decouvertAutoriseOverride;
    private BigDecimal decouvertParDefautOverride;
    private Boolean isActive;
    private Integer ordreAffichage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AccountSubType() {
    }

    public AccountSubType(UUID id, UUID accountTypeId, String code, String libelle, String description,
                          Boolean peutEmettreChequesOverride, Boolean peutRecevoirChequesOverride,
                          Boolean peutTransactionsEspecesOverride, Boolean decouvertAutoriseOverride,
                          BigDecimal decouvertParDefautOverride, Boolean isActive, Integer ordreAffichage,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.accountTypeId = accountTypeId;
        this.code = code;
        this.libelle = libelle;
        this.description = description;
        this.peutEmettreChequesOverride = peutEmettreChequesOverride;
        this.peutRecevoirChequesOverride = peutRecevoirChequesOverride;
        this.peutTransactionsEspecesOverride = peutTransactionsEspecesOverride;
        this.decouvertAutoriseOverride = decouvertAutoriseOverride;
        this.decouvertParDefautOverride = decouvertParDefautOverride;
        this.isActive = isActive;
        this.ordreAffichage = ordreAffichage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAccountTypeId() {
        return accountTypeId;
    }

    public void setAccountTypeId(UUID accountTypeId) {
        this.accountTypeId = accountTypeId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getPeutEmettreChequesOverride() {
        return peutEmettreChequesOverride;
    }

    public void setPeutEmettreChequesOverride(Boolean peutEmettreChequesOverride) {
        this.peutEmettreChequesOverride = peutEmettreChequesOverride;
    }

    public Boolean getPeutRecevoirChequesOverride() {
        return peutRecevoirChequesOverride;
    }

    public void setPeutRecevoirChequesOverride(Boolean peutRecevoirChequesOverride) {
        this.peutRecevoirChequesOverride = peutRecevoirChequesOverride;
    }

    public Boolean getPeutTransactionsEspecesOverride() {
        return peutTransactionsEspecesOverride;
    }

    public void setPeutTransactionsEspecesOverride(Boolean peutTransactionsEspecesOverride) {
        this.peutTransactionsEspecesOverride = peutTransactionsEspecesOverride;
    }

    public Boolean getDecouvertAutoriseOverride() {
        return decouvertAutoriseOverride;
    }

    public void setDecouvertAutoriseOverride(Boolean decouvertAutoriseOverride) {
        this.decouvertAutoriseOverride = decouvertAutoriseOverride;
    }

    public BigDecimal getDecouvertParDefautOverride() {
        return decouvertParDefautOverride;
    }

    public void setDecouvertParDefautOverride(BigDecimal decouvertParDefautOverride) {
        this.decouvertParDefautOverride = decouvertParDefautOverride;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getOrdreAffichage() {
        return ordreAffichage;
    }

    public void setOrdreAffichage(Integer ordreAffichage) {
        this.ordreAffichage = ordreAffichage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Effective value computation methods

    public Boolean getEffectivePeutEmettreChecques(Boolean parentValue) {
        return peutEmettreChequesOverride != null ? peutEmettreChequesOverride : parentValue;
    }

    public Boolean getEffectivePeutRecevoirChecques(Boolean parentValue) {
        return peutRecevoirChequesOverride != null ? peutRecevoirChequesOverride : parentValue;
    }

    public Boolean getEffectivePeutTransactionsEspeces(Boolean parentValue) {
        return peutTransactionsEspecesOverride != null ? peutTransactionsEspecesOverride : parentValue;
    }

    public Boolean getEffectiveDecouvertAutorise(Boolean parentValue) {
        return decouvertAutoriseOverride != null ? decouvertAutoriseOverride : parentValue;
    }

    public BigDecimal getEffectiveDecouvertParDefaut(BigDecimal parentValue) {
        return decouvertParDefautOverride != null ? decouvertParDefautOverride : parentValue;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID accountTypeId;
        private String code;
        private String libelle;
        private String description;
        private Boolean peutEmettreChequesOverride;
        private Boolean peutRecevoirChequesOverride;
        private Boolean peutTransactionsEspecesOverride;
        private Boolean decouvertAutoriseOverride;
        private BigDecimal decouvertParDefautOverride;
        private Boolean isActive;
        private Integer ordreAffichage;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder accountTypeId(UUID accountTypeId) {
            this.accountTypeId = accountTypeId;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder libelle(String libelle) {
            this.libelle = libelle;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder peutEmettreChequesOverride(Boolean peutEmettreChequesOverride) {
            this.peutEmettreChequesOverride = peutEmettreChequesOverride;
            return this;
        }

        public Builder peutRecevoirChequesOverride(Boolean peutRecevoirChequesOverride) {
            this.peutRecevoirChequesOverride = peutRecevoirChequesOverride;
            return this;
        }

        public Builder peutTransactionsEspecesOverride(Boolean peutTransactionsEspecesOverride) {
            this.peutTransactionsEspecesOverride = peutTransactionsEspecesOverride;
            return this;
        }

        public Builder decouvertAutoriseOverride(Boolean decouvertAutoriseOverride) {
            this.decouvertAutoriseOverride = decouvertAutoriseOverride;
            return this;
        }

        public Builder decouvertParDefautOverride(BigDecimal decouvertParDefautOverride) {
            this.decouvertParDefautOverride = decouvertParDefautOverride;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder ordreAffichage(Integer ordreAffichage) {
            this.ordreAffichage = ordreAffichage;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public AccountSubType build() {
            return new AccountSubType(id, accountTypeId, code, libelle, description,
                    peutEmettreChequesOverride, peutRecevoirChequesOverride,
                    peutTransactionsEspecesOverride, decouvertAutoriseOverride,
                    decouvertParDefautOverride, isActive, ordreAffichage, createdAt, updatedAt);
        }
    }
}
