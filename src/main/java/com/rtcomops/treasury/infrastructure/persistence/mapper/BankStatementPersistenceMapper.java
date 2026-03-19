package com.rtcomops.treasury.infrastructure.persistence.mapper;

import com.rtcomops.treasury.domain.model.BankStatement;
import com.rtcomops.treasury.infrastructure.persistence.entity.BankStatementEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between BankStatement domain model and persistence entity.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Component
public class BankStatementPersistenceMapper {

    /**
     * Converts an entity to a domain model.
     *
     * @param entity the persistence entity
     * @return the domain model
     */
    public BankStatement toDomain(BankStatementEntity entity) {
        if (entity == null) {
            return null;
        }

        return BankStatement.builder()
            .id(entity.getId())
            .bankAccountId(entity.getBankAccountId())
            .reference(entity.getReference())
            .statementDate(entity.getStatementDate())
            .periodStart(entity.getPeriodStart())
            .periodEnd(entity.getPeriodEnd())
            .openingBalance(entity.getOpeningBalance())
            .closingBalance(entity.getClosingBalance())
            .totalCredits(entity.getTotalCredits())
            .totalDebits(entity.getTotalDebits())
            .lineCount(entity.getLineCount())
            .reconciledCount(entity.getReconciledCount())
            .status(entity.getStatus())
            .importSource(entity.getImportSource())
            .fileName(entity.getFileName())
            .notes(entity.getNotes())
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
    public BankStatementEntity toEntity(BankStatement domain) {
        if (domain == null) {
            return null;
        }

        return BankStatementEntity.builder()
            .id(domain.getId())
            .bankAccountId(domain.getBankAccountId())
            .reference(domain.getReference())
            .statementDate(domain.getStatementDate())
            .periodStart(domain.getPeriodStart())
            .periodEnd(domain.getPeriodEnd())
            .openingBalance(domain.getOpeningBalance())
            .closingBalance(domain.getClosingBalance())
            .totalCredits(domain.getTotalCredits())
            .totalDebits(domain.getTotalDebits())
            .lineCount(domain.getLineCount())
            .reconciledCount(domain.getReconciledCount())
            .status(domain.getStatus())
            .importSource(domain.getImportSource())
            .fileName(domain.getFileName())
            .notes(domain.getNotes())
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
    public BankStatementEntity toEntityForUpdate(BankStatement domain) {
        BankStatementEntity entity = toEntity(domain);
        if (entity != null) {
            entity.markNotNew();
        }
        return entity;
    }
}
