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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a single line from a bank statement.
 *
 * <p>This entity maps to the treasury.statement_lines table and contains
 * individual transaction entries from bank statements.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "treasury", name = "statement_lines")
public class StatementLine implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("bank_statement_id")
    private UUID bankStatementId;

    @Column("line_number")
    private Integer lineNumber;

    @Column("transaction_date")
    private LocalDate transactionDate;

    @Column("value_date")
    private LocalDate valueDate;

    @Column("amount")
    private BigDecimal amount;

    @Column("direction")
    private String direction;

    @Column("reference")
    private String reference;

    @Column("description")
    private String description;

    @Column("partner_name")
    private String partnerName;

    @Column("partner_account")
    private String partnerAccount;

    @Column("balance_after")
    private BigDecimal balanceAfter;

    @Column("reconciliation_status")
    private String reconciliationStatus;

    @Column("reconciled_at")
    private LocalDateTime reconciledAt;

    @Column("raw_data")
    private String rawData;

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

    public StatementLine markNotNew() {
        this.isNew = false;
        return this;
    }
}
