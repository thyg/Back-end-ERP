package com.rtcomops.treasury.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for checkbook statistics response.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckbookStatsResponse {

    /** Nombre de cheques restants dans le chequier */
    private int remainingChecks;

    /** Nombre total de cheques utilises */
    private int usedChecks;

    /** Nombre de cheques emis (statut ISSUED) */
    private int issuedCount;

    /** Nombre de cheques encaisses (statut CASHED) */
    private int cashedCount;

    /** Nombre de cheques annules (statut CANCELLED) */
    private int cancelledCount;

    /** Montant total des cheques emis */
    private BigDecimal totalIssuedAmount;

    /** Montant total des cheques encaisses */
    private BigDecimal totalCashedAmount;
}
