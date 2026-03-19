package com.rtcomops.treasury.infrastructure.persistence.mapper;

import com.rtcomops.treasury.domain.model.AccountConnectorType;
import com.rtcomops.treasury.infrastructure.persistence.entity.AccountConnectorTypeEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between AccountConnectorType domain model and persistence entity.
 */
@Component
public class AccountConnectorTypePersistenceMapper {

    public AccountConnectorType toDomain(AccountConnectorTypeEntity entity) {
        if (entity == null) {
            return null;
        }
        return AccountConnectorType.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .bankCategoryId(entity.getBankCategoryId())
                .build();
    }

    public AccountConnectorTypeEntity toEntity(AccountConnectorType domain) {
        if (domain == null) {
            return null;
        }
        return AccountConnectorTypeEntity.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .name(domain.getName())
                .bankCategoryId(domain.getBankCategoryId())
                .isNew(domain.getId() == null)
                .build();
    }

    public AccountConnectorTypeEntity toEntityForUpdate(AccountConnectorType domain) {
        AccountConnectorTypeEntity entity = toEntity(domain);
        if (entity != null) {
            entity.setNew(false);
        }
        return entity;
    }
}
