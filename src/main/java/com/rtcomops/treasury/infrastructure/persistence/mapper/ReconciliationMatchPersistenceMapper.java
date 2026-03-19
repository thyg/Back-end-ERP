package com.rtcomops.treasury.infrastructure.persistence.mapper;

import com.rtcomops.treasury.domain.model.ReconciliationMatch;
import com.rtcomops.treasury.infrastructure.persistence.entity.ReconciliationMatchEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between ReconciliationMatch domain model and persistence entity.
 */
@Component
public class ReconciliationMatchPersistenceMapper {

    public ReconciliationMatch toDomain(ReconciliationMatchEntity entity) {
        if (entity == null) {
            return null;
        }
        return ReconciliationMatch.builder()
                .id(entity.getId())
                .statementLineId(entity.getStatementLineId())
                .bankTransactionId(entity.getBankTransactionId())
                .checkId(entity.getCheckId())
                .matchType(entity.getMatchType())
                .matchMethod(entity.getMatchMethod())
                .matchedAmount(entity.getMatchedAmount())
                .confidenceScore(entity.getConfidenceScore())
                .notes(entity.getNotes())
                .matchedBy(entity.getMatchedBy())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public ReconciliationMatchEntity toEntity(ReconciliationMatch domain) {
        if (domain == null) {
            return null;
        }
        return ReconciliationMatchEntity.builder()
                .id(domain.getId())
                .statementLineId(domain.getStatementLineId())
                .bankTransactionId(domain.getBankTransactionId())
                .checkId(domain.getCheckId())
                .matchType(domain.getMatchType())
                .matchMethod(domain.getMatchMethod())
                .matchedAmount(domain.getMatchedAmount())
                .confidenceScore(domain.getConfidenceScore())
                .notes(domain.getNotes())
                .matchedBy(domain.getMatchedBy())
                .createdAt(domain.getCreatedAt())
                .isNew(domain.getId() == null)
                .build();
    }

    public ReconciliationMatchEntity toEntityForUpdate(ReconciliationMatch domain) {
        ReconciliationMatchEntity entity = toEntity(domain);
        if (entity != null) {
            entity.setNew(false);
        }
        return entity;
    }
}
