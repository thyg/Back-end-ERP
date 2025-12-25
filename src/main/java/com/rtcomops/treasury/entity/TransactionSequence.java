package com.rtcomops.treasury.entity;

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
 * Entity representing a transaction sequence counter.
 *
 * <p>This entity maps to the treasury.transaction_sequences table and manages
 * auto-incrementing sequence numbers for transaction references by type and month.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "treasury", name = "transaction_sequences")
public class TransactionSequence implements Persistable<UUID> {

    /**
     * Unique identifier for the sequence record.
     */
    @Id
    private UUID id;

    /**
     * Transaction type code (e.g., "VIREMENT", "CHEQUE_EMIS").
     */
    @Column("type_code")
    private String typeCode;

    /**
     * Year and month in YYYYMM format.
     */
    @Column("year_month")
    private String yearMonth;

    /**
     * Last sequence number used for this type/month combination.
     */
    @Column("last_sequence")
    private Integer lastSequence;

    /**
     * Transient flag to indicate if this is a new entity.
     */
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

    public TransactionSequence markNotNew() {
        this.isNew = false;
        return this;
    }
}
