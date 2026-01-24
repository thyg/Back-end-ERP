package com.rtcomops.treasury.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for account sub-type response data.
 *
 * <p>Used to transfer account sub-type data to the client.
 * Includes both override values and effective values (computed from parent).</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSubTypeResponse {

    private UUID id;
    private UUID accountTypeId;
    private String code;
    private String libelle;
    private String description;

    // Override values (null means inherit from parent)
    private Boolean peutEmettreChequesOverride;
    private Boolean peutRecevoirChequesOverride;
    private Boolean peutTransactionsEspecesOverride;
    private Boolean decouvertAutoriseOverride;
    private BigDecimal decouvertParDefautOverride;

    // Effective values (computed from parent + overrides)
    private Boolean peutEmettreChecques;
    private Boolean peutRecevoirChecques;
    private Boolean peutTransactionsEspeces;
    private Boolean decouvertAutorise;
    private BigDecimal decouvertParDefaut;

    private Boolean isActive;
    private Integer ordreAffichage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
