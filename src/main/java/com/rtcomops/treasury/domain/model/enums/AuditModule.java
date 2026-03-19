package com.rtcomops.treasury.domain.model.enums;

/**
 * Modules du systeme de tresorerie pour l'audit.
 * Chaque module correspond a une entite principale.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-13
 */
public enum AuditModule {

    /** Gestion des etablissements bancaires */
    BANK("Banques"),

    /** Gestion des comptes bancaires */
    BANK_ACCOUNT("Comptes bancaires"),

    /** Types de transactions */
    TRANSACTION_TYPE("Types de transactions"),

    /** Transactions bancaires manuelles */
    BANK_TRANSACTION("Transactions bancaires"),

    /** Gestion des cheques */
    CHECK("Cheques"),

    /** Remises de cheques en lot */
    CHECK_DEPOSIT("Remises de cheques"),

    /** Gestion des chequiers */
    CHECKBOOK("Chequiers"),

    /** Releves bancaires */
    BANK_STATEMENT("Releves bancaires"),

    /** Lignes de releves */
    STATEMENT_LINE("Lignes de releves"),

    /** Rapprochement bancaire */
    RECONCILIATION("Rapprochement");

    private final String label;

    AuditModule(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
