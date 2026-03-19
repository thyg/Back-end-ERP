package com.rtcomops.treasury.domain.model.enums;

/**
 * Actions auditables dans le systeme de tresorerie.
 * Couvre toutes les operations CRUD et metier.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-13
 */
public enum AuditAction {

    // === CRUD Standard ===
    /** Creation d'une entite */
    CREATE("Creation", "success"),

    /** Mise a jour d'une entite */
    UPDATE("Modification", "warning"),

    /** Suppression d'une entite */
    DELETE("Suppression", "danger"),

    /** Consultation d'une entite (optionnel) */
    VIEW("Consultation", "info"),

    // === Actions d'etat ===
    /** Activation d'une entite */
    ACTIVATE("Activation", "success"),

    /** Desactivation d'une entite */
    DEACTIVATE("Desactivation", "warning"),

    // === Actions Transactions ===
    /** Validation d'une transaction */
    VALIDATE("Validation", "success"),

    /** Annulation d'une transaction */
    CANCEL("Annulation", "danger"),

    // === Actions Cheques ===
    /** Remise en banque d'un cheque */
    DEPOSIT("Remise en banque", "info"),

    /** Encaissement d'un cheque */
    CASH("Encaissement", "success"),

    /** Rejet d'un cheque */
    REJECT("Rejet", "danger"),

    // === Actions Releves ===
    /** Import d'un releve */
    IMPORT("Import", "info"),

    /** Cloture d'un releve */
    CLOSE("Cloture", "success"),

    // === Actions Rapprochement ===
    /** Rapprochement d'une remise de cheques */
    RECONCILE("Rapprochement remise", "success"),

    /** Rapprochement manuel */
    MANUAL_MATCH("Rapprochement manuel", "success"),

    /** Rapprochement automatique */
    AUTO_MATCH("Rapprochement auto", "success"),

    /** Annulation de rapprochement */
    UNMATCH("De-rapprochement", "warning"),

    /** Ignorer une ligne */
    IGNORE("Ignorer", "secondary"),

    /** Reinitialiser une ligne */
    RESET("Reinitialiser", "info");

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
