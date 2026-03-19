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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Persistence entity for CheckDeposit (batch check deposit).
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "treasury", name = "check_deposits")
public class CheckDepositEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("reference")
    private String reference;

    @Column("deposit_date")
    private LocalDate depositDate;

    @Column("bank_account_id")
    private UUID bankAccountId;

    @Column("total_amount")
    private BigDecimal totalAmount;

    @Column("check_count")
    private Integer checkCount;

    @Column("status")
    private String status;

    @Column("cashed_date")
    private LocalDate cashedDate;

    @Column("bank_transaction_id")
    private UUID bankTransactionId;

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

    public CheckDepositEntity markNotNew() {
        this.isNew = false;
        return this;
    }
}
