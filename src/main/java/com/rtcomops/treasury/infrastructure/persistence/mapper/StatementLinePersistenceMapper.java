package com.rtcomops.treasury.infrastructure.persistence.mapper;

import com.rtcomops.treasury.domain.model.StatementLine;
import com.rtcomops.treasury.infrastructure.persistence.entity.StatementLineEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between StatementLine domain model and persistence entity.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Component
public class StatementLinePersistenceMapper {

    /**
     * Converts an entity to a domain model.
     *
     * @param entity the persistence entity
     * @return the domain model
     */
    public StatementLine toDomain(StatementLineEntity entity) {
        if (entity == null) {
            return null;
        }

        return StatementLine.builder()
            .id(entity.getId())
            .bankStatementId(entity.getBankStatementId())
            .lineNumber(entity.getLineNumber())
            .transactionDate(entity.getTransactionDate())
            .valueDate(entity.getValueDate())
            .amount(entity.getAmount())
            .direction(entity.getDirection())
            .reference(entity.getReference())
            .description(entity.getDescription())
            .partnerName(entity.getPartnerName())
            .partnerAccount(entity.getPartnerAccount())
            .balanceAfter(entity.getBalanceAfter())
            .reconciliationStatus(entity.getReconciliationStatus())
            .reconciledAt(entity.getReconciledAt())
            .rawData(entity.getRawData())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    /**
     * Converts a domain model to an entity for insert.
     *
     * @param domain the domain model
     * @return the persistence entity
     */
    public StatementLineEntity toEntity(StatementLine domain) {
        if (domain == null) {
            return null;
        }

        return StatementLineEntity.builder()
            .id(domain.getId())
            .bankStatementId(domain.getBankStatementId())
            .lineNumber(domain.getLineNumber())
            .transactionDate(domain.getTransactionDate())
            .valueDate(domain.getValueDate())
            .amount(domain.getAmount())
            .direction(domain.getDirection())
            .reference(domain.getReference())
            .description(domain.getDescription())
            .partnerName(domain.getPartnerName())
            .partnerAccount(domain.getPartnerAccount())
            .balanceAfter(domain.getBalanceAfter())
            .reconciliationStatus(domain.getReconciliationStatus())
            .reconciledAt(domain.getReconciledAt())
            .rawData(domain.getRawData())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .isNew(true)
            .build();
    }

    /**
     * Converts a domain model to an entity for update.
     *
     * @param domain the domain model
     * @return the persistence entity marked as not new
     */
    public StatementLineEntity toEntityForUpdate(StatementLine domain) {
        StatementLineEntity entity = toEntity(domain);
        if (entity != null) {
            entity.markNotNew();
        }
        return entity;
    }
}
