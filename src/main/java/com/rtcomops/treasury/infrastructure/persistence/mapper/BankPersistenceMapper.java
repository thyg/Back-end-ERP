package com.rtcomops.treasury.infrastructure.persistence.mapper;

import com.rtcomops.treasury.domain.model.Bank;
import com.rtcomops.treasury.infrastructure.persistence.entity.BankEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Bank domain model and BankEntity persistence model.
 *
 * <p>This mapper is used by the persistence adapter to convert between
 * the pure domain model and the R2DBC entity.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@Component
public class BankPersistenceMapper {

    /**
     * Converts a Bank domain model to a BankEntity for persistence.
     * The entity is marked as new for INSERT operations.
     *
     * @param domain the domain model
     * @return the persistence entity
     */
    public BankEntity toEntity(Bank domain) {
        if (domain == null) {
            return null;
        }

        return BankEntity.builder()
            .id(domain.getId())
            .code(domain.getCode())
            .name(domain.getName())
            .swiftCode(domain.getSwiftCode())
            .bankCode(domain.getBankCode())
            .country(domain.getCountry())
            .address(domain.getAddress())
            .isActive(domain.getIsActive())
            .bankCategoryId(domain.getBankCategoryId())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .isNew(true)
            .build();
    }

    /**
     * Converts a Bank domain model to a BankEntity for update operations.
     * The entity is marked as NOT new for UPDATE operations.
     *
     * @param domain the domain model
     * @return the persistence entity marked for update
     */
    public BankEntity toEntityForUpdate(Bank domain) {
        if (domain == null) {
            return null;
        }

        return BankEntity.builder()
            .id(domain.getId())
            .code(domain.getCode())
            .name(domain.getName())
            .swiftCode(domain.getSwiftCode())
            .bankCode(domain.getBankCode())
            .country(domain.getCountry())
            .address(domain.getAddress())
            .isActive(domain.getIsActive())
            .bankCategoryId(domain.getBankCategoryId())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .isNew(false)
            .build();
    }

    /**
     * Converts a BankEntity persistence model to a Bank domain model.
     *
     * @param entity the persistence entity
     * @return the domain model
     */
    public Bank toDomain(BankEntity entity) {
        if (entity == null) {
            return null;
        }

        return Bank.builder()
            .id(entity.getId())
            .code(entity.getCode())
            .name(entity.getName())
            .swiftCode(entity.getSwiftCode())
            .bankCode(entity.getBankCode())
            .country(entity.getCountry())
            .address(entity.getAddress())
            .isActive(entity.getIsActive())
            .bankCategoryId(entity.getBankCategoryId())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
