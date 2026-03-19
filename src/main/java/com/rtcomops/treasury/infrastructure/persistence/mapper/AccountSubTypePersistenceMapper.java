package com.rtcomops.treasury.infrastructure.persistence.mapper;

import com.rtcomops.treasury.domain.model.AccountSubType;
import com.rtcomops.treasury.infrastructure.persistence.entity.AccountSubTypeEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between AccountSubType domain model and persistence entity.
 */
@Component
public class AccountSubTypePersistenceMapper {

    public AccountSubType toDomain(AccountSubTypeEntity entity) {
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

    public AccountSubTypeEntity toEntity(AccountSubType domain) {
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
                .isNew(domain.getId() == null)
                .build();
    }

    public AccountSubTypeEntity toEntityForUpdate(AccountSubType domain) {
        AccountSubTypeEntity entity = toEntity(domain);
        if (entity != null) {
            entity.setNew(false);
        }
        return entity;
    }
}
