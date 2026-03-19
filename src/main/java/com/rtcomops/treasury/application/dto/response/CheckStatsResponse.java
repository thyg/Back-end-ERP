package com.rtcomops.treasury.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for aggregated check statistics.
 * Provides KPIs for the checks dashboard.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckStatsResponse {

    // --- Totaux ---
    /** Nombre total de cheques (hors annules) */
    private int totalChecks;

    /** Montant total de tous les cheques */
    private BigDecimal totalAmount;

    // --- Cheques en attente (PENDING) ---
    /** Nombre de cheques en attente */
    private int pendingCount;

    /** Montant total des cheques en attente */
    private BigDecimal pendingAmount;

    // --- Cheques emis non encore debites (ISSUED) ---
    /** Nombre de cheques emis (en circulation, non debites) */
    private int issuedCount;

    /** Montant total des cheques emis en circulation */
    private BigDecimal issuedAmount;

    // --- Cheques recus (RECEIVED) ---
    /** Nombre de cheques recus (en main, non deposes) */
    private int receivedCount;

    /** Montant total des cheques recus en main */
    private BigDecimal receivedAmount;

    // --- Cheques remis en banque (DEPOSITED) ---
    /** Nombre de cheques deposes en attente d'encaissement */
    private int depositedCount;

    /** Montant total des cheques deposes */
    private BigDecimal depositedAmount;

    // --- Cheques en cours de traitement (IN_PROGRESS) ---
    /** Nombre de cheques en cours de traitement bancaire */
    private int inProgressCount;

    /** Montant des cheques en cours de traitement */
    private BigDecimal inProgressAmount;

    // --- Cheques encaisses (CASHED) ---
    /** Nombre de cheques encaisses/debites */
    private int cashedCount;

    /** Montant total encaisse/debite */
    private BigDecimal cashedAmount;

    // --- Cheques rejetes (REJECTED) ---
    /** Nombre de cheques rejetes */
    private int rejectedCount;

    /** Montant total des cheques rejetes */
    private BigDecimal rejectedAmount;

    // --- Cheques dont l'echeance est depassee ---
    /** Nombre de cheques avec date d'echeance depassee (non encaisses) */
    private int overdueCount;

    /** Montant total des cheques en retard */
    private BigDecimal overdueAmount;

    // --- Statistiques par type ---
    /** Nombre total de cheques emis (type ISSUED) */
    private int totalIssuedTypeCount;

    /** Montant total des cheques de type emis */
    private BigDecimal totalIssuedTypeAmount;

    /** Nombre total de cheques recus (type RECEIVED) */
    private int totalReceivedTypeCount;

    /** Montant total des cheques de type recu */
    private BigDecimal totalReceivedTypeAmount;
}
