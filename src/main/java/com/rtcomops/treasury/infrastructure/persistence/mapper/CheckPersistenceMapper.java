package com.rtcomops.treasury.infrastructure.persistence.mapper;

import com.rtcomops.treasury.domain.model.Check;
import com.rtcomops.treasury.infrastructure.persistence.entity.CheckEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Check domain model and CheckEntity persistence entity.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Component
public class CheckPersistenceMapper {

    /**
     * Converts a Check domain model to a CheckEntity for INSERT.
     *
     * @param domain the domain model
     * @return the persistence entity with isNew=true
     */
    public CheckEntity toEntity(Check domain) {
        if (domain == null) {
            return null;
        }

        return CheckEntity.builder()
                .id(domain.getId())
                .bankAccountId(domain.getBankAccountId())
                .checkType(domain.getCheckType())
                .checkNumber(domain.getCheckNumber())
                .amount(domain.getAmount())
                .partnerName(domain.getPartnerName())
                .issueDate(domain.getIssueDate())
                .dueDate(domain.getDueDate())
                .depositDate(domain.getDepositDate())
                .cashedDate(domain.getCashedDate())
                .receiptDate(domain.getReceiptDate())
                .emitDate(domain.getEmitDate())
                .status(domain.getStatus())
                .description(domain.getDescription())
                .rejectionReason(domain.getRejectionReason())
                .bankTransactionId(domain.getBankTransactionId())
                .checkbookId(domain.getCheckbookId())
                .referenceCode(domain.getReferenceCode())
                .imageUrl(domain.getImageUrl())
                .issuerBank(domain.getIssuerBank())
                .checkDepositId(domain.getCheckDepositId())
                .amountInWords(domain.getAmountInWords())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .isNew(true)
                .build();
    }

    /**
     * Converts a Check domain model to a CheckEntity for UPDATE.
     *
     * @param domain the domain model
     * @return the persistence entity with isNew=false
     */
    public CheckEntity toEntityForUpdate(Check domain) {
        if (domain == null) {
            return null;
        }

        return CheckEntity.builder()
                .id(domain.getId())
                .bankAccountId(domain.getBankAccountId())
                .checkType(domain.getCheckType())
                .checkNumber(domain.getCheckNumber())
                .amount(domain.getAmount())
                .partnerName(domain.getPartnerName())
                .issueDate(domain.getIssueDate())
                .dueDate(domain.getDueDate())
                .depositDate(domain.getDepositDate())
                .cashedDate(domain.getCashedDate())
                .receiptDate(domain.getReceiptDate())
                .emitDate(domain.getEmitDate())
                .status(domain.getStatus())
                .description(domain.getDescription())
                .rejectionReason(domain.getRejectionReason())
                .bankTransactionId(domain.getBankTransactionId())
                .checkbookId(domain.getCheckbookId())
                .referenceCode(domain.getReferenceCode())
                .imageUrl(domain.getImageUrl())
                .issuerBank(domain.getIssuerBank())
                .checkDepositId(domain.getCheckDepositId())
                .amountInWords(domain.getAmountInWords())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .isNew(false)
                .build();
    }

    /**
     * Converts a CheckEntity persistence entity to a Check domain model.
     *
     * @param entity the persistence entity
     * @return the domain model
     */
    public Check toDomain(CheckEntity entity) {
        if (entity == null) {
            return null;
        }

        return Check.builder()
                .id(entity.getId())
                .bankAccountId(entity.getBankAccountId())
                .checkType(entity.getCheckType())
                .checkNumber(entity.getCheckNumber())
                .amount(entity.getAmount())
                .partnerName(entity.getPartnerName())
                .issueDate(entity.getIssueDate())
                .dueDate(entity.getDueDate())
                .depositDate(entity.getDepositDate())
                .cashedDate(entity.getCashedDate())
                .receiptDate(entity.getReceiptDate())
                .emitDate(entity.getEmitDate())
                .status(entity.getStatus())
                .description(entity.getDescription())
                .rejectionReason(entity.getRejectionReason())
                .bankTransactionId(entity.getBankTransactionId())
                .checkbookId(entity.getCheckbookId())
                .referenceCode(entity.getReferenceCode())
                .imageUrl(entity.getImageUrl())
                .issuerBank(entity.getIssuerBank())
                .checkDepositId(entity.getCheckDepositId())
                .amountInWords(entity.getAmountInWords())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
