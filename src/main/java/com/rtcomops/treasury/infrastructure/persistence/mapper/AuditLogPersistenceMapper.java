package com.rtcomops.treasury.infrastructure.persistence.mapper;

import com.rtcomops.treasury.domain.model.AuditLog;
import com.rtcomops.treasury.infrastructure.persistence.entity.AuditLogEntity;
import io.r2dbc.postgresql.codec.Json;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between AuditLog domain model and persistence entity.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-13
 */
@Component
public class AuditLogPersistenceMapper {

    /**
     * Converts an entity to a domain model.
     *
     * @param entity the persistence entity
     * @return the domain model
     */
    public AuditLog toDomain(AuditLogEntity entity) {
        if (entity == null) {
            return null;
        }

        return AuditLog.builder()
            .id(entity.getId())
            .module(entity.getModule())
            .action(entity.getAction())
            .entityId(entity.getEntityId())
            .entityReference(entity.getEntityReference())
            .userId(entity.getUserId())
            .userName(entity.getUserName())
            .oldValue(jsonToString(entity.getOldValue()))
            .newValue(jsonToString(entity.getNewValue()))
            .ipAddress(entity.getIpAddress())
            .userAgent(entity.getUserAgent())
            .description(entity.getDescription())
            .createdAt(entity.getCreatedAt())
            .build();
    }

    /**
     * Converts a domain model to an entity.
     *
     * @param domain the domain model
     * @return the persistence entity
     */
    public AuditLogEntity toEntity(AuditLog domain) {
        if (domain == null) {
            return null;
        }

        return AuditLogEntity.builder()
            .id(domain.getId())
            .module(domain.getModule())
            .action(domain.getAction())
            .entityId(domain.getEntityId())
            .entityReference(domain.getEntityReference())
            .userId(domain.getUserId())
            .userName(domain.getUserName())
            .oldValue(stringToJson(domain.getOldValue()))
            .newValue(stringToJson(domain.getNewValue()))
            .ipAddress(domain.getIpAddress())
            .userAgent(domain.getUserAgent())
            .description(domain.getDescription())
            .createdAt(domain.getCreatedAt())
            .isNew(true)
            .build();
    }

    /**
     * Converts a Json object to a String.
     */
    private String jsonToString(Json json) {
        return json != null ? json.asString() : null;
    }

    /**
     * Converts a String to a Json object.
     */
    private Json stringToJson(String value) {
        return value != null ? Json.of(value) : null;
    }
}
