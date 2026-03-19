package com.rtcomops.treasury.infrastructure.persistence.mapper;

import com.rtcomops.treasury.domain.model.TransactionSequence;
import com.rtcomops.treasury.infrastructure.persistence.entity.TransactionSequenceEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between TransactionSequence domain model and TransactionSequenceEntity persistence model.
 *
 * <p>This mapper is used by the persistence adapter to convert between
 * the pure domain model and the R2DBC entity.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
@Component
public class TransactionSequencePersistenceMapper {

    /**
     * Converts a TransactionSequence domain model to a TransactionSequenceEntity for persistence.
     * The entity is marked as new for INSERT operations.
     *
     * @param domain the domain model
     * @return the persistence entity
     */
    public TransactionSequenceEntity toEntity(TransactionSequence domain) {
        if (domain == null) {
            return null;
        }

        return TransactionSequenceEntity.builder()
                .id(domain.getId())
                .typeCode(domain.getTypeCode())
                .yearMonth(domain.getYearMonth())
                .lastSequence(domain.getLastSequence())
                .isNew(true)
                .build();
    }

    /**
     * Converts a TransactionSequence domain model to a TransactionSequenceEntity for update operations.
     * The entity is marked as NOT new for UPDATE operations.
     *
     * @param domain the domain model
     * @return the persistence entity marked for update
     */
    public TransactionSequenceEntity toEntityForUpdate(TransactionSequence domain) {
        if (domain == null) {
            return null;
        }

        return TransactionSequenceEntity.builder()
                .id(domain.getId())
                .typeCode(domain.getTypeCode())
                .yearMonth(domain.getYearMonth())
                .lastSequence(domain.getLastSequence())
                .isNew(false)
                .build();
    }

    /**
     * Converts a TransactionSequenceEntity persistence model to a TransactionSequence domain model.
     *
     * @param entity the persistence entity
     * @return the domain model
     */
    public TransactionSequence toDomain(TransactionSequenceEntity entity) {
        if (entity == null) {
            return null;
        }

        return TransactionSequence.builder()
                .id(entity.getId())
                .typeCode(entity.getTypeCode())
                .yearMonth(entity.getYearMonth())
                .lastSequence(entity.getLastSequence())
                .build();
    }
}
