package com.rtcomops.treasury.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for account type response data.
 *
 * <p>Used to transfer account type data to the client.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountTypeResponse {

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

    /**
     * List of sub-types for this account type.
     * Only populated when specifically requested.
     */
    private List<AccountSubTypeResponse> subTypes;
}
