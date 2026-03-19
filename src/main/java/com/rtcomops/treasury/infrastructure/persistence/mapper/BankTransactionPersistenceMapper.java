package com.rtcomops.treasury.infrastructure.persistence.mapper;

import com.rtcomops.treasury.domain.model.BankTransaction;
import com.rtcomops.treasury.infrastructure.persistence.entity.BankTransactionEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between BankTransaction domain model and BankTransactionEntity persistence model.
 *
 * <p>This mapper is used by the persistence adapter to convert between
 * the pure domain model and the R2DBC entity.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Component
public class BankTransactionPersistenceMapper {

    /**
     * Converts a BankTransaction domain model to a BankTransactionEntity for persistence.
     * The entity is marked as new for INSERT operations.
     *
     * @param domain the domain model
     * @return the persistence entity
     */
    public BankTransactionEntity toEntity(BankTransaction domain) {
        if (domain == null) {
            return null;
        }

        return BankTransactionEntity.builder()
                .id(domain.getId())
                .bankAccountId(domain.getBankAccountId())
                .transactionTypeId(domain.getTransactionTypeId())
                .reference(domain.getReference())
                .transactionDate(domain.getTransactionDate())
                .valueDate(domain.getValueDate())
                .amount(domain.getAmount())
                .direction(domain.getDirection())
                .description(domain.getDescription())
                .partnerName(domain.getPartnerName())
                .status(domain.getStatus())
                .systemDate(domain.getSystemDate())
                .isReconciled(domain.getIsReconciled())
                .reconciledAt(domain.getReconciledAt())
                .statementLineId(domain.getStatementLineId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .isNew(true)
                .build();
    }

    /**
     * Converts a BankTransaction domain model to a BankTransactionEntity for update operations.
     * The entity is marked as NOT new for UPDATE operations.
     *
     * @param domain the domain model
     * @return the persistence entity marked for update
     */
    public BankTransactionEntity toEntityForUpdate(BankTransaction domain) {
        if (domain == null) {
            return null;
        }

        return BankTransactionEntity.builder()
                .id(domain.getId())
                .bankAccountId(domain.getBankAccountId())
                .transactionTypeId(domain.getTransactionTypeId())
                .reference(domain.getReference())
                .transactionDate(domain.getTransactionDate())
                .valueDate(domain.getValueDate())
                .amount(domain.getAmount())
                .direction(domain.getDirection())
                .description(domain.getDescription())
                .partnerName(domain.getPartnerName())
                .status(domain.getStatus())
                .systemDate(domain.getSystemDate())
                .isReconciled(domain.getIsReconciled())
                .reconciledAt(domain.getReconciledAt())
                .statementLineId(domain.getStatementLineId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .isNew(false)
                .build();
    }

    /**
     * Converts a BankTransactionEntity persistence model to a BankTransaction domain model.
     *
     * @param entity the persistence entity
     * @return the domain model
     */
    public BankTransaction toDomain(BankTransactionEntity entity) {
        if (entity == null) {
            return null;
        }

        return BankTransaction.builder()
                .id(entity.getId())
                .bankAccountId(entity.getBankAccountId())
                .transactionTypeId(entity.getTransactionTypeId())
                .reference(entity.getReference())
                .transactionDate(entity.getTransactionDate())
                .valueDate(entity.getValueDate())
                .amount(entity.getAmount())
                .direction(entity.getDirection())
                .description(entity.getDescription())
                .partnerName(entity.getPartnerName())
                .status(entity.getStatus())
                .systemDate(entity.getSystemDate())
                .isReconciled(entity.getIsReconciled())
                .reconciledAt(entity.getReconciledAt())
                .statementLineId(entity.getStatementLineId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
