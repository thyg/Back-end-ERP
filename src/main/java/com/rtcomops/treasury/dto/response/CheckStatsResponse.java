package com.rtcomops.treasury.dto.response;

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
    /** Nombre total de chèques (hors annulés) */
    private int totalChecks;

    /** Montant total de tous les chèques */
    private BigDecimal totalAmount;

    // --- Chèques en attente (PENDING) ---
    /** Nombre de chèques en attente */
    private int pendingCount;

    /** Montant total des chèques en attente */
    private BigDecimal pendingAmount;

    // --- Chèques émis non encore débités (ISSUED) ---
    /** Nombre de chèques émis (en circulation, non débités) */
    private int issuedCount;

    /** Montant total des chèques émis en circulation */
    private BigDecimal issuedAmount;

    // --- Chèques reçus (RECEIVED) ---
    /** Nombre de chèques reçus (en main, non déposés) */
    private int receivedCount;

    /** Montant total des chèques reçus en main */
    private BigDecimal receivedAmount;

    // --- Chèques remis en banque (DEPOSITED) ---
    /** Nombre de chèques déposés en attente d'encaissement */
    private int depositedCount;

    /** Montant total des chèques déposés */
    private BigDecimal depositedAmount;

    // --- Chèques en cours de traitement (IN_PROGRESS) ---
    /** Nombre de chèques en cours de traitement bancaire */
    private int inProgressCount;

    /** Montant des chèques en cours de traitement */
    private BigDecimal inProgressAmount;

    // --- Chèques encaissés (CASHED) ---
    /** Nombre de chèques encaissés/débités */
    private int cashedCount;

    /** Montant total encaissé/débité */
    private BigDecimal cashedAmount;

    // --- Chèques rejetés (REJECTED) ---
    /** Nombre de chèques rejetés */
    private int rejectedCount;

    /** Montant total des chèques rejetés */
    private BigDecimal rejectedAmount;

    // --- Chèques dont l'échéance est dépassée ---
    /** Nombre de chèques avec date d'échéance dépassée (non encaissés) */
    private int overdueCount;

    /** Montant total des chèques en retard */
    private BigDecimal overdueAmount;

    // --- Statistiques par type ---
    /** Nombre total de chèques émis (type ISSUED) */
    private int totalIssuedTypeCount;

    /** Montant total des chèques de type émis */
    private BigDecimal totalIssuedTypeAmount;

    /** Nombre total de chèques reçus (type RECEIVED) */
    private int totalReceivedTypeCount;

    /** Montant total des chèques de type reçu */
    private BigDecimal totalReceivedTypeAmount;
}
