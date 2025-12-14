package com.rtcomops.treasury.entity;

import io.r2dbc.postgresql.codec.Json;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an audit log entry for treasury operations.
 *
 * <p>This entity maps to the treasury.audit_logs table and contains
 * a complete trace of all operations performed in the treasury module.</p>
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
@Table(schema = "treasury", name = "audit_logs")
public class AuditLog implements Persistable<UUID> {

    /**
     * Unique identifier for the audit log entry.
     */
    @Id
    private UUID id;

    /**
     * Module where the action occurred.
     * Values: BANK, BANK_ACCOUNT, TRANSACTION_TYPE, BANK_TRANSACTION,
     *         CHECK, BANK_STATEMENT, STATEMENT_LINE, RECONCILIATION
     */
    @Column("module")
    private String module;

    /**
     * Action performed.
     * Values: CREATE, UPDATE, DELETE, VALIDATE, CANCEL, DEPOSIT, CASH,
     *         REJECT, IMPORT, CLOSE, MANUAL_MATCH, AUTO_MATCH, UNMATCH, etc.
     */
    @Column("action")
    private String action;

    /**
     * UUID of the affected entity.
     */
    @Column("entity_id")
    private UUID entityId;

    /**
     * Human-readable reference of the entity (e.g., "CHQ-001234", "VIR-2024-001").
     */
    @Column("entity_reference")
    private String entityReference;

    /**
     * ID of the user who performed the action (null if system).
     */
    @Column("user_id")
    private UUID userId;

    /**
     * Name of the user who performed the action.
     */
    @Column("user_name")
    private String userName;

    /**
     * JSON representation of the entity BEFORE the change.
     * NULL for CREATE actions.
     */
    @Column("old_value")
    private Json oldValue;

    /**
     * JSON representation of the entity AFTER the change.
     * NULL for DELETE actions.
     */
    @Column("new_value")
    private Json newValue;

    /**
     * IP address of the client.
     */
    @Column("ip_address")
    private String ipAddress;

    /**
     * User agent string of the client.
     */
    @Column("user_agent")
    private String userAgent;

    /**
     * Human-readable description of the action.
     */
    @Column("description")
    private String description;

    /**
     * Timestamp when the action was performed.
     */
    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    /**
     * Transient flag to indicate if this is a new entity.
     */
    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return this.id;
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    public AuditLog markNotNew() {
        this.isNew = false;
        return this;
    }
}