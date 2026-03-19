package com.rtcomops.treasury.infrastructure.persistence.mapper;

import com.rtcomops.treasury.domain.model.CheckDeposit;
import com.rtcomops.treasury.infrastructure.persistence.entity.CheckDepositEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between CheckDeposit domain model and persistence entity.
 */
@Component
public class CheckDepositPersistenceMapper {

    public CheckDeposit toDomain(CheckDepositEntity entity) {
        if (entity == null) {
            return null;
        }
        return CheckDeposit.builder()
                .id(entity.getId())
                .reference(entity.getReference())
                .depositDate(entity.getDepositDate())
                .bankAccountId(entity.getBankAccountId())
                .totalAmount(entity.getTotalAmount())
                .checkCount(entity.getCheckCount())
                .status(entity.getStatus())
                .cashedDate(entity.getCashedDate())
                .bankTransactionId(entity.getBankTransactionId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public CheckDepositEntity toEntity(CheckDeposit domain) {
        if (domain == null) {
            return null;
        }
        return CheckDepositEntity.builder()
                .id(domain.getId())
                .reference(domain.getReference())
                .depositDate(domain.getDepositDate())
                .bankAccountId(domain.getBankAccountId())
                .totalAmount(domain.getTotalAmount())
                .checkCount(domain.getCheckCount())
                .status(domain.getStatus())
                .cashedDate(domain.getCashedDate())
                .bankTransactionId(domain.getBankTransactionId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .isNew(domain.getId() == null)
                .build();
    }

    public CheckDepositEntity toEntityForUpdate(CheckDeposit domain) {
        CheckDepositEntity entity = toEntity(domain);
        if (entity != null) {
            entity.setNew(false);
        }
        return entity;
    }
}
