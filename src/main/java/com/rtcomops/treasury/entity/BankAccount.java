package com.rtcomops.treasury.entity;

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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a company bank account.
 *
 * <p>This entity maps to the treasury.bank_accounts table and contains
 * information about bank accounts used for treasury operations.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "treasury", name = "bank_accounts")
public class BankAccount implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("bank_id")
    private UUID bankId;

    @Column("name")
    private String name;

    @Column("account_number")
    private String accountNumber;

    @Column("iban")
    private String iban;

    @Column("bic")
    private String bic;

    @Column("currency")
    private String currency;

    @Column("current_balance")
    private BigDecimal currentBalance;

    @Column("reconciled_balance")
    private BigDecimal reconciledBalance;

    @Column("is_active")
    private Boolean isActive;

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

    public BankAccount markNotNew() {
        this.isNew = false;
        return this;
    }
}
