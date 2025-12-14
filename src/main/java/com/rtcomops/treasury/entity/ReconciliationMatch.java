package com.rtcomops.treasury.entity;

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
 * Entity representing a reconciliation match between a statement line and internal records.
 *
 * <p>This entity maps to the treasury.reconciliation_matches table and contains
 * the links between bank statement lines and internal transactions or checks.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "treasury", name = "reconciliation_matches")
public class ReconciliationMatch implements Persistable<UUID> {

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

    public ReconciliationMatch markNotNew() {
        this.isNew = false;
        return this;
    }
}
