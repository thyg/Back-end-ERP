package com.rtcomops.treasury.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import io.r2dbc.postgresql.codec.Json;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Persistence entity for BankAccount.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "treasury", name = "bank_accounts")
public class BankAccountEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("bank_id")
    private UUID bankId;

    @Column("account_type_id")
    private UUID accountTypeId;

    @Column("account_sub_type_id")
    private UUID accountSubTypeId;

    @Column("name")
    private String name;

    @Column("branch_code")
    private String branchCode;

    @Column("generated_iban")
    private String generatedIban;

    @Column("currency")
    private String currency;

    @Column("current_balance")
    private BigDecimal currentBalance;

    @Column("reconciled_balance")
    private BigDecimal reconciledBalance;

    @Column("is_active")
    private Boolean isActive;

    @Column("connector_type_id")
    private UUID connectorTypeId;

    @Column("details")
    private Json details;

    @Column("overdraft_authorized")
    private Boolean overdraftAuthorized;

    @Column("overdraft_limit")
    private BigDecimal overdraftLimit;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

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

    public BankAccountEntity markNotNew() {
        this.isNew = false;
        return this;
    }
}
