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
 * Persistence entity for TransactionSequence.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "treasury", name = "transaction_sequences")
public class TransactionSequenceEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("type_code")
    private String typeCode;

    @Column("year_month")
    private String yearMonth;

    @Column("last_sequence")
    private Integer lastSequence;

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

    public TransactionSequenceEntity markNotNew() {
        this.isNew = false;
        return this;
    }
}
