package com.rtcomops.treasury.enums;

/**
 * Actions auditables dans le système de trésorerie.
 * Couvre toutes les opérations CRUD et métier.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-13
 */
public enum AuditAction {
    
    // === CRUD Standard ===
    /** Création d'une entité */
    CREATE("Création", "success"),
    
    /** Mise à jour d'une entité */
    UPDATE("Modification", "warning"),
    
    /** Suppression d'une entité */
    DELETE("Suppression", "danger"),
    
    /** Consultation d'une entité (optionnel) */
    VIEW("Consultation", "info"),
    
    // === Actions d'état ===
    /** Activation d'une entité */
    ACTIVATE("Activation", "success"),
    
    /** Désactivation d'une entité */
    DEACTIVATE("Désactivation", "warning"),
    
    // === Actions Transactions ===
    /** Validation d'une transaction */
    VALIDATE("Validation", "success"),
    
    /** Annulation d'une transaction */
    CANCEL("Annulation", "danger"),
    
    // === Actions Chèques ===
    /** Remise en banque d'un chèque */
    DEPOSIT("Remise en banque", "info"),
    
    /** Encaissement d'un chèque */
    CASH("Encaissement", "success"),
    
    /** Rejet d'un chèque */
    REJECT("Rejet", "danger"),
    
    // === Actions Relevés ===
    /** Import d'un relevé */
    IMPORT("Import", "info"),
    
    /** Clôture d'un relevé */
    CLOSE("Clôture", "success"),
    
    // === Actions Rapprochement ===
    /** Rapprochement manuel */
    MANUAL_MATCH("Rapprochement manuel", "success"),
    
    /** Rapprochement automatique */
    AUTO_MATCH("Rapprochement auto", "success"),
    
    /** Annulation de rapprochement */
    UNMATCH("Dé-rapprochement", "warning"),
    
    /** Ignorer une ligne */
    IGNORE("Ignorer", "secondary"),
    
    /** Réinitialiser une ligne */
    RESET("Réinitialiser", "info");
    
    private final String label;
    private final String severity;
    
    AuditAction(String label, String severity) {
        this.label = label;
        this.severity = severity;
    }
    
    public String getLabel() {
        return label;
    }
    
    public String getSeverity() {
        return severity;
    }
}