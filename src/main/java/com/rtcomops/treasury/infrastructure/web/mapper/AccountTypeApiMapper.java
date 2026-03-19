package com.rtcomops.treasury.infrastructure.web.mapper;

import com.rtcomops.treasury.application.dto.request.CreateAccountTypeRequest;
import com.rtcomops.treasury.application.dto.request.UpdateAccountTypeRequest;
import com.rtcomops.treasury.application.dto.response.AccountSubTypeResponse;
import com.rtcomops.treasury.application.dto.response.AccountTypeResponse;
import com.rtcomops.treasury.domain.model.AccountSubType;
import com.rtcomops.treasury.domain.model.AccountType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper for converting between AccountType/AccountSubType domain models and web DTOs.
 *
 * <p>This mapper is used by the web controller to convert between
 * request/response DTOs and the domain model.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Component
public class AccountTypeApiMapper {

    /**
     * Converts a CreateAccountTypeRequest DTO to an AccountType domain model.
     * Sets ID, timestamps, and defaults.
     *
     * @param request the create request DTO
     * @return an AccountType domain model ready for persistence
     */
    public AccountType toEntity(CreateAccountTypeRequest request) {
        if (request == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();

        return AccountType.builder()
                .id(UUID.randomUUID())
                .code(request.getCode().toUpperCase())
                .libelle(request.getLibelle())
                .description(request.getDescription())
                .peutEmettreChecques(request.getPeutEmettreChecques() != null ? request.getPeutEmettreChecques() : false)
                .peutRecevoirChecques(request.getPeutRecevoirChecques() != null ? request.getPeutRecevoirChecques() : false)
                .peutTransactionsEspeces(request.getPeutTransactionsEspeces() != null ? request.getPeutTransactionsEspeces() : false)
                .decouvertAutorise(request.getDecouvertAutorise() != null ? request.getDecouvertAutorise() : false)
                .decouvertParDefaut(request.getDecouvertParDefaut() != null ? request.getDecouvertParDefaut() : BigDecimal.ZERO)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .ordreAffichage(request.getOrdreAffichage() != null ? request.getOrdreAffichage() : 0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Updates an existing AccountType domain model with values from UpdateAccountTypeRequest.
     *
     * @param existing the existing domain model
     * @param request the update request DTO
     * @return the updated AccountType domain model
     */
    public AccountType updateEntity(AccountType existing, UpdateAccountTypeRequest request) {
        if (request == null) {
            return existing;
        }

        if (request.getCode() != null) {
            existing.setCode(request.getCode().toUpperCase());
        }
        if (request.getLibelle() != null) {
            existing.setLibelle(request.getLibelle());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getPeutEmettreChecques() != null) {
            existing.setPeutEmettreChecques(request.getPeutEmettreChecques());
        }
        if (request.getPeutRecevoirChecques() != null) {
            existing.setPeutRecevoirChecques(request.getPeutRecevoirChecques());
        }
        if (request.getPeutTransactionsEspeces() != null) {
            existing.setPeutTransactionsEspeces(request.getPeutTransactionsEspeces());
        }
        if (request.getDecouvertAutorise() != null) {
            existing.setDecouvertAutorise(request.getDecouvertAutorise());
        }
        if (request.getDecouvertParDefaut() != null) {
            existing.setDecouvertParDefaut(request.getDecouvertParDefaut());
        }
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }
        if (request.getOrdreAffichage() != null) {
            existing.setOrdreAffichage(request.getOrdreAffichage());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        return existing;
    }

    /**
     * Converts an AccountType domain model to an AccountTypeResponse DTO.
     *
     * @param entity the AccountType domain model
     * @return the response DTO
     */
    public AccountTypeResponse toResponse(AccountType entity) {
        if (entity == null) {
            return null;
        }

        return AccountTypeResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .libelle(entity.getLibelle())
                .description(entity.getDescription())
                .peutEmettreChecques(entity.getPeutEmettreChecques())
                .peutRecevoirChecques(entity.getPeutRecevoirChecques())
                .peutTransactionsEspeces(entity.getPeutTransactionsEspeces())
                .decouvertAutorise(entity.getDecouvertAutorise())
                .decouvertParDefaut(entity.getDecouvertParDefaut())
                .isActive(entity.getIsActive())
                .ordreAffichage(entity.getOrdreAffichage())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Converts an AccountSubType domain model to an AccountSubTypeResponse DTO.
     * Computes effective values from parent AccountType.
     *
     * @param entity the AccountSubType domain model
     * @param parent the parent AccountType domain model
     * @return the response DTO with effective values
     */
    public AccountSubTypeResponse toSubTypeResponse(AccountSubType entity, AccountType parent) {
        if (entity == null) {
            return null;
        }

        return AccountSubTypeResponse.builder()
                .id(entity.getId())
                .accountTypeId(entity.getAccountTypeId())
                .code(entity.getCode())
                .libelle(entity.getLibelle())
                .description(entity.getDescription())
                // Override values
                .peutEmettreChequesOverride(entity.getPeutEmettreChequesOverride())
                .peutRecevoirChequesOverride(entity.getPeutRecevoirChequesOverride())
                .peutTransactionsEspecesOverride(entity.getPeutTransactionsEspecesOverride())
                .decouvertAutoriseOverride(entity.getDecouvertAutoriseOverride())
                .decouvertParDefautOverride(entity.getDecouvertParDefautOverride())
                // Effective values (computed)
                .peutEmettreChecques(entity.getEffectivePeutEmettreChecques(parent.getPeutEmettreChecques()))
                .peutRecevoirChecques(entity.getEffectivePeutRecevoirChecques(parent.getPeutRecevoirChecques()))
                .peutTransactionsEspeces(entity.getEffectivePeutTransactionsEspeces(parent.getPeutTransactionsEspeces()))
                .decouvertAutorise(entity.getEffectiveDecouvertAutorise(parent.getDecouvertAutorise()))
                .decouvertParDefaut(entity.getEffectiveDecouvertParDefaut(parent.getDecouvertParDefaut()))
                // Other fields
                .isActive(entity.getIsActive())
                .ordreAffichage(entity.getOrdreAffichage())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
