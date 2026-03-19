package com.rtcomops.treasury.infrastructure.persistence.entity;

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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Persistence entity for ReconciliationMatch.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "treasury", name = "reconciliation_matches")
public class ReconciliationMatchEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("statement_line_id")
    private UUID statementLineId;

    @Column("bank_transaction_id")
    private UUID bankTransactionId;

    @Column("check_id")
    private UUID checkId;

    @Column("match_type")
    private String matchType;

    @Column("match_method")
    private String matchMethod;

    @Column("matched_amount")
    private BigDecimal matchedAmount;

    @Column("confidence_score")
    private BigDecimal confidenceScore;

    @Column("notes")
    private String notes;

    @Column("matched_by")
    private String matchedBy;

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

    public ReconciliationMatchEntity markNotNew() {
        this.isNew = false;
        return this;
    }
}
