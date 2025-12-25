package com.rtcomops.treasury.enums;

/**
 * Modules du système de trésorerie pour l'audit.
 * Chaque module correspond à une entité principale.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-13
 */
public enum AuditModule {
    
    /** Gestion des établissements bancaires */
    BANK("Banques"),
    
    /** Gestion des comptes bancaires */
    BANK_ACCOUNT("Comptes bancaires"),
    
    /** Types de transactions */
    TRANSACTION_TYPE("Types de transactions"),
    
    /** Transactions bancaires manuelles */
    BANK_TRANSACTION("Transactions bancaires"),
    
    /** Gestion des chèques */
    CHECK("Chèques"),

    /** Gestion des chéquiers */
    CHECKBOOK("Chéquiers"),

    /** Relevés bancaires */
    BANK_STATEMENT("Relevés bancaires"),
    
    /** Lignes de relevés */
    STATEMENT_LINE("Lignes de relevés"),
    
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