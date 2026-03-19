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
 * Persistence entity for BankTransaction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "treasury", name = "bank_transactions")
public class BankTransactionEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("bank_account_id")
    private UUID bankAccountId;

    @Column("transaction_type_id")
    private UUID transactionTypeId;

    @Column("reference")
    private String reference;

    @Column("transaction_date")
    private LocalDate transactionDate;

    @Column("value_date")
    private LocalDate valueDate;

    @Column("amount")
    private BigDecimal amount;

    @Column("direction")
    private String direction;

    @Column("description")
    private String description;

    @Column("partner_name")
    private String partnerName;

    @Column("status")
    private String status;

    @Column("system_date")
    private LocalDateTime systemDate;

    @Column("is_reconciled")
    private Boolean isReconciled;

    @Column("reconciled_at")
    private LocalDateTime reconciledAt;

    @Column("statement_line_id")
    private UUID statementLineId;

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

    public BankTransactionEntity markNotNew() {
        this.isNew = false;
        return this;
    }
}
