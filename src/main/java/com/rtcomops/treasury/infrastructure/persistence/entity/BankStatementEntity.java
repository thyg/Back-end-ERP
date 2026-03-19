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
 * Persistence entity for BankStatement.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "treasury", name = "bank_statements")
public class BankStatementEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("bank_account_id")
    private UUID bankAccountId;

    @Column("reference")
    private String reference;

    @Column("statement_date")
    private LocalDate statementDate;

    @Column("period_start")
    private LocalDate periodStart;

    @Column("period_end")
    private LocalDate periodEnd;

    @Column("opening_balance")
    private BigDecimal openingBalance;

    @Column("closing_balance")
    private BigDecimal closingBalance;

    @Column("total_credits")
    private BigDecimal totalCredits;

    @Column("total_debits")
    private BigDecimal totalDebits;

    @Column("line_count")
    private Integer lineCount;

    @Column("reconciled_count")
    private Integer reconciledCount;

    @Column("status")
    private String status;

    @Column("import_source")
    private String importSource;

    @Column("file_name")
    private String fileName;

    @Column("notes")
    private String notes;

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

    public BankStatementEntity markNotNew() {
        this.isNew = false;
        return this;
    }
}
