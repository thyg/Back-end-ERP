package com.rtcomops.treasury.infrastructure.persistence.mapper;

import com.rtcomops.treasury.domain.model.BankCategory;
import com.rtcomops.treasury.infrastructure.persistence.entity.BankCategoryEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between BankCategory domain model and persistence entity.
 */
@Component
public class BankCategoryPersistenceMapper {

    public BankCategory toDomain(BankCategoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return BankCategory.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .label(entity.getLabel())
                .build();
    }

    public BankCategoryEntity toEntity(BankCategory domain) {
        if (domain == null) {
            return null;
        }
        return BankCategoryEntity.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .label(domain.getLabel())
                .isNew(domain.getId() == null)
                .build();
    }

    public BankCategoryEntity toEntityForUpdate(BankCategory domain) {
        BankCategoryEntity entity = toEntity(domain);
        if (entity != null) {
            entity.setNew(false);
        }
        return entity;
    }
}
