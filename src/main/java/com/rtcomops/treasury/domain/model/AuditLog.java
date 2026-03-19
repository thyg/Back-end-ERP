package com.rtcomops.treasury.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model representing an audit log entry for treasury operations.
 *
 * <p>This is a pure POJO without any framework dependencies.
 * It captures a complete trace of all operations performed in the treasury module.</p>
 *
 * <p>Each entry captures:</p>
 * <ul>
 *   <li>WHO: userId, userName</li>
 *   <li>WHAT: module, action, entityId, entityReference</li>
 *   <li>WHEN: createdAt</li>
 *   <li>CHANGES: oldValue (JSON), newValue (JSON)</li>
 *   <li>CONTEXT: ipAddress, userAgent, description</li>
 * </ul>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    /**
     * Unique identifier for the audit log entry.
     */
    private UUID id;

    /**
     * Module where the action occurred.
     * Values: BANK, BANK_ACCOUNT, TRANSACTION_TYPE, BANK_TRANSACTION,
     *         CHECK, BANK_STATEMENT, STATEMENT_LINE, RECONCILIATION
     */
    private String module;

    /**
     * Action performed.
     * Values: CREATE, UPDATE, DELETE, VALIDATE, CANCEL, DEPOSIT, CASH,
     *         REJECT, IMPORT, CLOSE, MANUAL_MATCH, AUTO_MATCH, UNMATCH, etc.
     */
    private String action;

    /**
     * UUID of the affected entity.
     */
    private UUID entityId;

    /**
     * Human-readable reference of the entity (e.g., "CHQ-001234", "VIR-2024-001").
     */
    private String entityReference;

    /**
     * ID of the user who performed the action (null if system).
     */
    private UUID userId;

    /**
     * Name of the user who performed the action.
     */
    private String userName;

    /**
     * JSON representation of the entity BEFORE the change.
     * NULL for CREATE actions.
     */
    private String oldValue;

    /**
     * JSON representation of the entity AFTER the change.
     * NULL for DELETE actions.
     */
    private String newValue;

    /**
     * IP address of the client.
     */
    private String ipAddress;

    /**
     * User agent string of the client.
     */
    private String userAgent;

    /**
     * Human-readable description of the action.
     */
    private String description;

    /**
     * Timestamp when the action was performed.
     */
    private LocalDateTime createdAt;
}
