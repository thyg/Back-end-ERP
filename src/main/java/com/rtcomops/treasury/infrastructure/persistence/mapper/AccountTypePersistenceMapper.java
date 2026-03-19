package com.rtcomops.treasury.infrastructure.persistence.mapper;

import com.rtcomops.treasury.domain.model.AccountSubType;
import com.rtcomops.treasury.domain.model.AccountType;
import com.rtcomops.treasury.infrastructure.persistence.entity.AccountSubTypeEntity;
import com.rtcomops.treasury.infrastructure.persistence.entity.AccountTypeEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between AccountType/AccountSubType domain models and persistence entities.
 *
 * <p>This mapper is used by the persistence adapter to convert between
 * the pure domain models and the R2DBC entities.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Component
public class AccountTypePersistenceMapper {

    // ========== AccountType Mapping ==========

    /**
     * Converts an AccountType domain model to an AccountTypeEntity for persistence.
     * The entity is marked as new for INSERT operations.
     *
     * @param domain the domain model
     * @return the persistence entity
     */
    public AccountTypeEntity toEntity(AccountType domain) {
        if (domain == null) {
            return null;
        }

        return AccountTypeEntity.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .libelle(domain.getLibelle())
                .description(domain.getDescription())
                .peutEmettreChecques(domain.getPeutEmettreChecques())
                .peutRecevoirChecques(domain.getPeutRecevoirChecques())
                .peutTransactionsEspeces(domain.getPeutTransactionsEspeces())
                .decouvertAutorise(domain.getDecouvertAutorise())
                .decouvertParDefaut(domain.getDecouvertParDefaut())
                .isActive(domain.getIsActive())
                .ordreAffichage(domain.getOrdreAffichage())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .isNew(true)
                .build();
    }

    /**
     * Converts an AccountType domain model to an AccountTypeEntity for update operations.
     * The entity is marked as NOT new for UPDATE operations.
     *
     * @param domain the domain model
     * @return the persistence entity marked for update
     */
    public AccountTypeEntity toEntityForUpdate(AccountType domain) {
        if (domain == null) {
            return null;
        }

        return AccountTypeEntity.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .libelle(domain.getLibelle())
                .description(domain.getDescription())
                .peutEmettreChecques(domain.getPeutEmettreChecques())
                .peutRecevoirChecques(domain.getPeutRecevoirChecques())
                .peutTransactionsEspeces(domain.getPeutTransactionsEspeces())
                .decouvertAutorise(domain.getDecouvertAutorise())
                .decouvertParDefaut(domain.getDecouvertParDefaut())
                .isActive(domain.getIsActive())
                .ordreAffichage(domain.getOrdreAffichage())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .isNew(false)
                .build();
    }

    /**
     * Converts an AccountTypeEntity persistence model to an AccountType domain model.
     *
     * @param entity the persistence entity
     * @return the domain model
     */
    public AccountType toDomain(AccountTypeEntity entity) {
        if (entity == null) {
            return null;
        }

        return AccountType.builder()
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

    // ========== AccountSubType Mapping ==========

    /**
     * Converts an AccountSubType domain model to an AccountSubTypeEntity for persistence.
     * The entity is marked as new for INSERT operations.
     *
     * @param domain the domain model
     * @return the persistence entity
     */
    public AccountSubTypeEntity toSubTypeEntity(AccountSubType domain) {
        if (domain == null) {
            return null;
        }

        return AccountSubTypeEntity.builder()
                .id(domain.getId())
                .accountTypeId(domain.getAccountTypeId())
                .code(domain.getCode())
                .libelle(domain.getLibelle())
                .description(domain.getDescription())
                .peutEmettreChequesOverride(domain.getPeutEmettreChequesOverride())
                .peutRecevoirChequesOverride(domain.getPeutRecevoirChequesOverride())
                .peutTransactionsEspecesOverride(domain.getPeutTransactionsEspecesOverride())
                .decouvertAutoriseOverride(domain.getDecouvertAutoriseOverride())
                .decouvertParDefautOverride(domain.getDecouvertParDefautOverride())
                .isActive(domain.getIsActive())
                .ordreAffichage(domain.getOrdreAffichage())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .isNew(true)
                .build();
    }

    /**
     * Converts an AccountSubType domain model to an AccountSubTypeEntity for update operations.
     * The entity is marked as NOT new for UPDATE operations.
     *
     * @param domain the domain model
     * @return the persistence entity marked for update
     */
    public AccountSubTypeEntity toSubTypeEntityForUpdate(AccountSubType domain) {
        if (domain == null) {
            return null;
        }

        return AccountSubTypeEntity.builder()
                .id(domain.getId())
                .accountTypeId(domain.getAccountTypeId())
                .code(domain.getCode())
                .libelle(domain.getLibelle())
                .description(domain.getDescription())
                .peutEmettreChequesOverride(domain.getPeutEmettreChequesOverride())
                .peutRecevoirChequesOverride(domain.getPeutRecevoirChequesOverride())
                .peutTransactionsEspecesOverride(domain.getPeutTransactionsEspecesOverride())
                .decouvertAutoriseOverride(domain.getDecouvertAutoriseOverride())
                .decouvertParDefautOverride(domain.getDecouvertParDefautOverride())
                .isActive(domain.getIsActive())
                .ordreAffichage(domain.getOrdreAffichage())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .isNew(false)
                .build();
    }

    /**
     * Converts an AccountSubTypeEntity persistence model to an AccountSubType domain model.
     *
     * @param entity the persistence entity
     * @return the domain model
     */
    public AccountSubType toSubTypeDomain(AccountSubTypeEntity entity) {
        if (entity == null) {
            return null;
        }

        return AccountSubType.builder()
                .id(entity.getId())
                .accountTypeId(entity.getAccountTypeId())
                .code(entity.getCode())
                .libelle(entity.getLibelle())
                .description(entity.getDescription())
                .peutEmettreChequesOverride(entity.getPeutEmettreChequesOverride())
                .peutRecevoirChequesOverride(entity.getPeutRecevoirChequesOverride())
                .peutTransactionsEspecesOverride(entity.getPeutTransactionsEspecesOverride())
                .decouvertAutoriseOverride(entity.getDecouvertAutoriseOverride())
                .decouvertParDefautOverride(entity.getDecouvertParDefautOverride())
                .isActive(entity.getIsActive())
                .ordreAffichage(entity.getOrdreAffichage())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
