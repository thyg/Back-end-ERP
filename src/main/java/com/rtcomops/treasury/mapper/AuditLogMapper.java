package com.rtcomops.treasury.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtcomops.treasury.dto.response.AuditLogResponse;
import com.rtcomops.treasury.entity.AuditLog;
import com.rtcomops.treasury.enums.AuditAction;
import com.rtcomops.treasury.enums.AuditModule;
import io.r2dbc.postgresql.codec.Json;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * Mapper for AuditLog entity and DTOs.
 *
 * <p>Provides conversion methods and utility functions for
 * creating audit log entries from various sources.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-13
 */
@Component
public class AuditLogMapper {

    private static final DateTimeFormatter FRENCH_DATE_FORMAT = 
        DateTimeFormatter.ofPattern("d MMM yyyy 'à' HH:mm", Locale.FRENCH);

    private final ObjectMapper objectMapper;

    public AuditLogMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Converts an AuditLog entity to a response DTO.
     */
    public AuditLogResponse toResponse(AuditLog entity) {
        String moduleLabel = getModuleLabel(entity.getModule());
        String actionLabel = getActionLabel(entity.getAction());
        String actionSeverity = getActionSeverity(entity.getAction());
        String oldValue = entity.getOldValue() != null ? entity.getOldValue().asString() : null;
        String newValue = entity.getNewValue() != null ? entity.getNewValue().asString() : null;

        return AuditLogResponse.builder()
            .id(entity.getId())
            // Module & Action
            .module(entity.getModule())
            .moduleLabel(moduleLabel)
            .action(entity.getAction())
            .actionLabel(actionLabel)
            .actionSeverity(actionSeverity)
            // Entity
            .entityId(entity.getEntityId())
            .entityReference(entity.getEntityReference())
            // User
            .userId(entity.getUserId())
            .userName(entity.getUserName() != null ? entity.getUserName() : "Système")
            // Changes
            .oldValue(oldValue)
            .newValue(newValue)
            .hasChanges(entity.getOldValue() != null || entity.getNewValue() != null)
            // Context
            .ipAddress(entity.getIpAddress())
            .userAgent(entity.getUserAgent())
            .description(entity.getDescription())
            // Timestamp
            .createdAt(entity.getCreatedAt())
            .formattedDate(formatDate(entity.getCreatedAt()))
            .relativeTime(getRelativeTime(entity.getCreatedAt()))
            .build();
    }

    /**
     * Creates a new AuditLog entity.
     */
    public AuditLog createEntity(
            AuditModule module,
            AuditAction action,
            UUID entityId,
            String entityReference,
            Object oldValue,
            Object newValue,
            String description,
            UUID userId,
            String userName,
            String ipAddress,
            String userAgent) {

        return AuditLog.builder()
            .id(UUID.randomUUID())
            .module(module.name())
            .action(action.name())
            .entityId(entityId)
            .entityReference(entityReference)
            .userId(userId)
            .userName(userName)
            .oldValue(toJson(oldValue))
            .newValue(toJson(newValue))
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .description(description)
            .createdAt(LocalDateTime.now())
            .isNew(true)
            .build();
    }

    /**
     * Converts an object to JSON string.
     */
    public Json toJson(Object value) {
        if (value == null) return null;
        try {
            return Json.of(objectMapper.writeValueAsString(value));
        } catch (JsonProcessingException e) {
            return Json.of("{\"error\": \"Serialization failed\"}");
        }
    }

    /**
     * Gets the French label for a module.
     */
    private String getModuleLabel(String module) {
        try {
            return AuditModule.valueOf(module).getLabel();
        } catch (IllegalArgumentException e) {
            return module;
        }
    }

    /**
     * Gets the French label for an action.
     */
    private String getActionLabel(String action) {
        try {
            return AuditAction.valueOf(action).getLabel();
        } catch (IllegalArgumentException e) {
            return action;
        }
    }

    /**
     * Gets the severity (color) for an action.
     */
    private String getActionSeverity(String action) {
        try {
            return AuditAction.valueOf(action).getSeverity();
        } catch (IllegalArgumentException e) {
            return "secondary";
        }
    }

    /**
     * Formats a date in French format.
     */
    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(FRENCH_DATE_FORMAT);
    }

    /**
     * Gets a relative time string (e.g., "Il y a 5 minutes").
     */
    private String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return "À l'instant";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return "Il y a " + minutes + " minute" + (minutes > 1 ? "s" : "");
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return "Il y a " + hours + " heure" + (hours > 1 ? "s" : "");
        } else if (seconds < 604800) {
            long days = seconds / 86400;
            return "Il y a " + days + " jour" + (days > 1 ? "s" : "");
        } else {
            return formatDate(dateTime);
        }
    }
}