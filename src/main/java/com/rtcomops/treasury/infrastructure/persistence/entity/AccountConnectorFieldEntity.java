package com.rtcomops.treasury.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Persistence entity for AccountConnectorField.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "treasury", name = "account_connector_fields")
public class AccountConnectorFieldEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("connector_type_id")
    private UUID connectorTypeId;

    @Column("field_key")
    private String fieldKey;

    @Column("label")
    private String label;

    @Column("field_type")
    private String fieldType;

    @Column("is_required")
    private Boolean isRequired;

    @Column("display_order")
    private Integer displayOrder;

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

    public AccountConnectorFieldEntity markNotNew() {
        this.isNew = false;
        return this;
    }
}
