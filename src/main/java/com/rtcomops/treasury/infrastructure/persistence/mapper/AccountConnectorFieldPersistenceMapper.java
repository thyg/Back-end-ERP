package com.rtcomops.treasury.infrastructure.persistence.mapper;

import com.rtcomops.treasury.domain.model.AccountConnectorField;
import com.rtcomops.treasury.infrastructure.persistence.entity.AccountConnectorFieldEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between AccountConnectorField domain model and persistence entity.
 */
@Component
public class AccountConnectorFieldPersistenceMapper {

    public AccountConnectorField toDomain(AccountConnectorFieldEntity entity) {
        if (entity == null) {
            return null;
        }
        return AccountConnectorField.builder()
                .id(entity.getId())
                .connectorTypeId(entity.getConnectorTypeId())
                .fieldKey(entity.getFieldKey())
                .label(entity.getLabel())
                .fieldType(entity.getFieldType())
                .isRequired(entity.getIsRequired())
                .displayOrder(entity.getDisplayOrder())
                .build();
    }

    public AccountConnectorFieldEntity toEntity(AccountConnectorField domain) {
        if (domain == null) {
            return null;
        }
        return AccountConnectorFieldEntity.builder()
                .id(domain.getId())
                .connectorTypeId(domain.getConnectorTypeId())
                .fieldKey(domain.getFieldKey())
                .label(domain.getLabel())
                .fieldType(domain.getFieldType())
                .isRequired(domain.getIsRequired())
                .displayOrder(domain.getDisplayOrder())
                .isNew(domain.getId() == null)
                .build();
    }

    public AccountConnectorFieldEntity toEntityForUpdate(AccountConnectorField domain) {
        AccountConnectorFieldEntity entity = toEntity(domain);
        if (entity != null) {
            entity.setNew(false);
        }
        return entity;
    }
}
