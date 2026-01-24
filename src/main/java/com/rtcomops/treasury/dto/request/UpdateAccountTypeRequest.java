package com.rtcomops.treasury.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for updating an existing account type.
 *
 * <p>All fields are optional. Only non-null fields will be updated.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountTypeRequest {

    /**
     * Unique code for the account type (2-20 characters).
     */
    @Size(min = 2, max = 20, message = "Le code doit contenir entre 2 et 20 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Le code ne doit contenir que des lettres, chiffres et underscores")
    private String code;

    /**
     * Human-readable label (2-100 characters).
     */
    @Size(min = 2, max = 100, message = "Le libelle doit contenir entre 2 et 100 caracteres")
    private String libelle;

    /**
     * Optional description of the account type.
     */
    @Size(max = 500, message = "La description ne doit pas depasser 500 caracteres")
    private String description;

    /**
     * Whether this account type can issue checks.
     */
    private Boolean peutEmettreChecques;

    /**
     * Whether this account type can receive checks.
     */
    private Boolean peutRecevoirChecques;

    /**
     * Whether this account type allows cash transactions.
     */
    private Boolean peutTransactionsEspeces;

    /**
     * Whether overdraft is authorized for this account type.
     */
    private Boolean decouvertAutorise;

    /**
     * Default overdraft limit for this account type.
     */
    @PositiveOrZero(message = "Le decouvert par defaut doit etre positif ou zero")
    private BigDecimal decouvertParDefaut;

    /**
     * Display order in lists.
     */
    @PositiveOrZero(message = "L'ordre d'affichage doit etre positif ou zero")
    private Integer ordreAffichage;

    /**
     * Whether the account type is active.
     */
    private Boolean isActive;
}
