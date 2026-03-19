package com.rtcomops.treasury.infrastructure.persistence.mapper;

import com.rtcomops.treasury.domain.model.TransactionType;
import com.rtcomops.treasury.infrastructure.persistence.entity.TransactionTypeEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between TransactionType domain model and TransactionTypeEntity persistence model.
 *
 * <p>This mapper is used by the persistence adapter to convert between
 * the pure domain model and the R2DBC entity.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@Component
public class TransactionTypePersistenceMapper {

    /**
     * Converts a TransactionType domain model to a TransactionTypeEntity for persistence.
     * The entity is marked as new for INSERT operations.
     *
     * @param domain the domain model
     * @return the persistence entity
     */
    public TransactionTypeEntity toEntity(TransactionType domain) {
        if (domain == null) {
            return null;
        }

        return TransactionTypeEntity.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .label(domain.getLabel())
                .category(domain.getCategory())
                .description(domain.getDescription())
                .isActive(domain.getIsActive())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .isNew(true)
                .build();
    }

    /**
     * Converts a TransactionType domain model to a TransactionTypeEntity for update operations.
     * The entity is marked as NOT new for UPDATE operations.
     *
     * @param domain the domain model
     * @return the persistence entity marked for update
     */
    public TransactionTypeEntity toEntityForUpdate(TransactionType domain) {
        if (domain == null) {
            return null;
        }

        return TransactionTypeEntity.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .label(domain.getLabel())
                .category(domain.getCategory())
                .description(domain.getDescription())
                .isActive(domain.getIsActive())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .isNew(false)
                .build();
    }

    /**
     * Converts a TransactionTypeEntity persistence model to a TransactionType domain model.
     *
     * @param entity the persistence entity
     * @return the domain model
     */
    public TransactionType toDomain(TransactionTypeEntity entity) {
        if (entity == null) {
            return null;
        }

        return TransactionType.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .label(entity.getLabel())
                .category(entity.getCategory())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
