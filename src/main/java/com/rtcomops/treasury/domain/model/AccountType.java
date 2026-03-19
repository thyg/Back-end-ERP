package com.rtcomops.treasury.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model representing an account type configuration.
 *
 * <p>This is a pure POJO without any framework annotations.
 * It defines the different types of bank accounts (e.g., CHEQUE, ESPECES, EPARGNE).</p>
 *
 * <p>Each account type has configurable permissions for checks and cash
 * transactions, as well as overdraft settings.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
public class AccountType {

    private UUID id;
    private String code;
    private String libelle;
    private String description;
    private Boolean peutEmettreChecques;
    private Boolean peutRecevoirChecques;
    private Boolean peutTransactionsEspeces;
    private Boolean decouvertAutorise;
    private BigDecimal decouvertParDefaut;
    private Boolean isActive;
    private Integer ordreAffichage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AccountType() {
    }

    public AccountType(UUID id, String code, String libelle, String description,
                       Boolean peutEmettreChecques, Boolean peutRecevoirChecques,
                       Boolean peutTransactionsEspeces, Boolean decouvertAutorise,
                       BigDecimal decouvertParDefaut, Boolean isActive, Integer ordreAffichage,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.code = code;
        this.libelle = libelle;
        this.description = description;
        this.peutEmettreChecques = peutEmettreChecques;
        this.peutRecevoirChecques = peutRecevoirChecques;
        this.peutTransactionsEspeces = peutTransactionsEspeces;
        this.decouvertAutorise = decouvertAutorise;
        this.decouvertParDefaut = decouvertParDefaut;
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

    public Boolean getPeutEmettreChecques() {
        return peutEmettreChecques;
    }

    public void setPeutEmettreChecques(Boolean peutEmettreChecques) {
        this.peutEmettreChecques = peutEmettreChecques;
    }

    public Boolean getPeutRecevoirChecques() {
        return peutRecevoirChecques;
    }

    public void setPeutRecevoirChecques(Boolean peutRecevoirChecques) {
        this.peutRecevoirChecques = peutRecevoirChecques;
    }

    public Boolean getPeutTransactionsEspeces() {
        return peutTransactionsEspeces;
    }

    public void setPeutTransactionsEspeces(Boolean peutTransactionsEspeces) {
        this.peutTransactionsEspeces = peutTransactionsEspeces;
    }

    public Boolean getDecouvertAutorise() {
        return decouvertAutorise;
    }

    public void setDecouvertAutorise(Boolean decouvertAutorise) {
        this.decouvertAutorise = decouvertAutorise;
    }

    public BigDecimal getDecouvertParDefaut() {
        return decouvertParDefaut;
    }

    public void setDecouvertParDefaut(BigDecimal decouvertParDefaut) {
        this.decouvertParDefaut = decouvertParDefaut;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String code;
        private String libelle;
        private String description;
        private Boolean peutEmettreChecques;
        private Boolean peutRecevoirChecques;
        private Boolean peutTransactionsEspeces;
        private Boolean decouvertAutorise;
        private BigDecimal decouvertParDefaut;
        private Boolean isActive;
        private Integer ordreAffichage;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) {
            this.id = id;
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

        public Builder peutEmettreChecques(Boolean peutEmettreChecques) {
            this.peutEmettreChecques = peutEmettreChecques;
            return this;
        }

        public Builder peutRecevoirChecques(Boolean peutRecevoirChecques) {
            this.peutRecevoirChecques = peutRecevoirChecques;
            return this;
        }

        public Builder peutTransactionsEspeces(Boolean peutTransactionsEspeces) {
            this.peutTransactionsEspeces = peutTransactionsEspeces;
            return this;
        }

        public Builder decouvertAutorise(Boolean decouvertAutorise) {
            this.decouvertAutorise = decouvertAutorise;
            return this;
        }

        public Builder decouvertParDefaut(BigDecimal decouvertParDefaut) {
            this.decouvertParDefaut = decouvertParDefaut;
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

        public AccountType build() {
            return new AccountType(id, code, libelle, description, peutEmettreChecques,
                    peutRecevoirChecques, peutTransactionsEspeces, decouvertAutorise,
                    decouvertParDefaut, isActive, ordreAffichage, createdAt, updatedAt);
        }
    }
}
