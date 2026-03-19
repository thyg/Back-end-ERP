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
 * Persistence entity for AccountConnectorType.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "treasury", name = "account_connector_types")
public class AccountConnectorTypeEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("code")
    private String code;

    @Column("name")
    private String name;

    @Column("bank_category_id")
    private UUID bankCategoryId;

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

    public AccountConnectorTypeEntity markNotNew() {
        this.isNew = false;
        return this;
    }
}
