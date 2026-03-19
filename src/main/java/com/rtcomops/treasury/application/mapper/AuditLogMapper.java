package com.rtcomops.treasury.application.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtcomops.treasury.application.dto.response.AuditLogResponse;
import com.rtcomops.treasury.domain.model.AuditLog;
import com.rtcomops.treasury.domain.model.enums.AuditAction;
import com.rtcomops.treasury.domain.model.enums.AuditModule;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * Mapper for AuditLog domain model and DTOs.
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
        DateTimeFormatter.ofPattern("d MMM yyyy 'a' HH:mm", Locale.FRENCH);

    private final ObjectMapper objectMapper;

    public AuditLogMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Converts an AuditLog domain model to a response DTO.
     *
     * @param domain the domain model
     * @return the response DTO
     */
    public AuditLogResponse toResponse(AuditLog domain) {
        String moduleLabel = getModuleLabel(domain.getModule());
        String actionLabel = getActionLabel(domain.getAction());
        String actionSeverity = getActionSeverity(domain.getAction());

        return AuditLogResponse.builder()
            .id(domain.getId())
            // Module & Action
            .module(domain.getModule())
            .moduleLabel(moduleLabel)
            .action(domain.getAction())
            .actionLabel(actionLabel)
            .actionSeverity(actionSeverity)
            // Entity
            .entityId(domain.getEntityId())
            .entityReference(domain.getEntityReference())
            // User
            .userId(domain.getUserId())
            .userName(domain.getUserName() != null ? domain.getUserName() : "Systeme")
            // Changes
            .oldValue(domain.getOldValue())
            .newValue(domain.getNewValue())
            .hasChanges(domain.getOldValue() != null || domain.getNewValue() != null)
            // Context
            .ipAddress(domain.getIpAddress())
            .userAgent(domain.getUserAgent())
            .description(domain.getDescription())
            // Timestamp
            .createdAt(domain.getCreatedAt())
            .formattedDate(formatDate(domain.getCreatedAt()))
            .relativeTime(getRelativeTime(domain.getCreatedAt()))
            .build();
    }

    /**
     * Creates a new AuditLog domain model.
     *
     * @param module The audit module
     * @param action The audit action
     * @param entityId The entity ID
     * @param entityReference The entity reference
     * @param oldValue The old value object
     * @param newValue The new value object
     * @param description The description
     * @param userId The user ID
     * @param userName The user name
     * @param ipAddress The IP address
     * @param userAgent The user agent
     * @return the domain model
     */
    public AuditLog toDomain(
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
            .build();
    }

    /**
     * Converts an object to JSON string.
     *
     * @param value the object to convert
     * @return JSON string or null
     */
    public String toJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Serialization failed\"}";
        }
    }

    /**
     * Gets the French label for a module.
     *
     * @param module the module name
     * @return the label
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
     *
     * @param action the action name
     * @return the label
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
     *
     * @param action the action name
     * @return the severity
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
     *
     * @param dateTime the date time
     * @return formatted string
     */
    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(FRENCH_DATE_FORMAT);
    }

    /**
     * Gets a relative time string (e.g., "Il y a 5 minutes").
     *
     * @param dateTime the date time
     * @return relative time string
     */
    private String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return "A l'instant";
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
