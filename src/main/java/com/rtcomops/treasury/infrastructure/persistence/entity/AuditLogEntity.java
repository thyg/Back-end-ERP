package com.rtcomops.treasury.infrastructure.persistence.entity;

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
 * Persistence entity for AuditLog.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "treasury", name = "audit_logs")
public class AuditLogEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("module")
    private String module;

    @Column("action")
    private String action;

    @Column("entity_id")
    private UUID entityId;

    @Column("entity_reference")
    private String entityReference;

    @Column("user_id")
    private UUID userId;

    @Column("user_name")
    private String userName;

    @Column("old_value")
    private Json oldValue;

    @Column("new_value")
    private Json newValue;

    @Column("ip_address")
    private String ipAddress;

    @Column("user_agent")
    private String userAgent;

    @Column("description")
    private String description;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

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

    public AuditLogEntity markNotNew() {
        this.isNew = false;
        return this;
    }
}
