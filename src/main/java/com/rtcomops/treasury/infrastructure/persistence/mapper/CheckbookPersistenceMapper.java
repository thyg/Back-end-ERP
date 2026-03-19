package com.rtcomops.treasury.infrastructure.persistence.mapper;

import com.rtcomops.treasury.domain.model.Checkbook;
import com.rtcomops.treasury.infrastructure.persistence.entity.CheckbookEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Checkbook domain model and CheckbookEntity persistence entity.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
@Component
public class CheckbookPersistenceMapper {

    /**
     * Converts a Checkbook domain model to a CheckbookEntity for INSERT.
     *
     * @param domain the domain model
     * @return the persistence entity with isNew=true
     */
    public CheckbookEntity toEntity(Checkbook domain) {
        if (domain == null) {
            return null;
        }

        return CheckbookEntity.builder()
                .id(domain.getId())
                .bankAccountId(domain.getBankAccountId())
                .iban(domain.getIban())
                .prefix(domain.getPrefix())
                .startNumber(domain.getStartNumber())
                .endNumber(domain.getEndNumber())
                .numberOfPages(domain.getNumberOfPages())
                .currentNumber(domain.getCurrentNumber())
                .status(domain.getStatus())
                .type(domain.getType())
                .isSystem(domain.getIsSystem())
                .nextSequence(domain.getNextSequence())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .isNew(true)
                .build();
    }

    /**
     * Converts a Checkbook domain model to a CheckbookEntity for UPDATE.
     *
     * @param domain the domain model
     * @return the persistence entity with isNew=false
     */
    public CheckbookEntity toEntityForUpdate(Checkbook domain) {
        if (domain == null) {
            return null;
        }

        return CheckbookEntity.builder()
                .id(domain.getId())
                .bankAccountId(domain.getBankAccountId())
                .iban(domain.getIban())
                .prefix(domain.getPrefix())
                .startNumber(domain.getStartNumber())
                .endNumber(domain.getEndNumber())
                .numberOfPages(domain.getNumberOfPages())
                .currentNumber(domain.getCurrentNumber())
                .status(domain.getStatus())
                .type(domain.getType())
                .isSystem(domain.getIsSystem())
                .nextSequence(domain.getNextSequence())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .isNew(false)
                .build();
    }

    /**
     * Converts a CheckbookEntity persistence entity to a Checkbook domain model.
     *
     * @param entity the persistence entity
     * @return the domain model
     */
    public Checkbook toDomain(CheckbookEntity entity) {
        if (entity == null) {
            return null;
        }

        return Checkbook.builder()
                .id(entity.getId())
                .bankAccountId(entity.getBankAccountId())
                .iban(entity.getIban())
                .prefix(entity.getPrefix())
                .startNumber(entity.getStartNumber())
                .endNumber(entity.getEndNumber())
                .numberOfPages(entity.getNumberOfPages())
                .currentNumber(entity.getCurrentNumber())
                .status(entity.getStatus())
                .type(entity.getType())
                .isSystem(entity.getIsSystem())
                .nextSequence(entity.getNextSequence())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
