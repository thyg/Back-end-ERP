package com.rtcomops.treasury.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for audit log API responses.
 *
 * <p>Contains all audit information plus computed/enriched fields
 * for better frontend display.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private UUID id;

    // === Module & Action ===
    private String module;
    private String moduleLabel;      // Label traduit (ex: "Transactions bancaires")
    private String action;
    private String actionLabel;      // Label traduit (ex: "Creation")
    private String actionSeverity;   // success, warning, danger, info

    // === Entity ===
    private UUID entityId;
    private String entityReference;

    // === User ===
    private UUID userId;
    private String userName;

    // === Changes ===
    private String oldValue;         // JSON string
    private String newValue;         // JSON string
    private boolean hasChanges;      // true if oldValue or newValue is not null

    // === Context ===
    private String ipAddress;
    private String userAgent;
    private String description;

    // === Timestamp ===
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    // === Computed fields for display ===
    private String formattedDate;    // "13 dec. 2024 a 10:30"
    private String relativeTime;     // "Il y a 5 minutes"
}
