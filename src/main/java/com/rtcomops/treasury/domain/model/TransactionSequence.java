package com.rtcomops.treasury.domain.model;

import java.util.UUID;

/**
 * Domain model representing a transaction sequence counter.
 *
 * <p>This is a pure POJO without any framework annotations.
 * It manages auto-incrementing sequence numbers for transaction references by type and month.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
public class TransactionSequence {

    /**
     * Unique identifier for the sequence record.
     */
    private UUID id;

    /**
     * Transaction type code (e.g., "VIREMENT", "CHEQUE_EMIS").
     */
    private String typeCode;

    /**
     * Year and month in YYYYMM format.
     */
    private String yearMonth;

    /**
     * Last sequence number used for this type/month combination.
     */
    private Integer lastSequence;

    /**
     * Default constructor.
     */
    public TransactionSequence() {
    }

    /**
     * All-args constructor.
     */
    public TransactionSequence(UUID id, String typeCode, String yearMonth, Integer lastSequence) {
        this.id = id;
        this.typeCode = typeCode;
        this.yearMonth = yearMonth;
        this.lastSequence = lastSequence;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }

    public Integer getLastSequence() {
        return lastSequence;
    }

    public void setLastSequence(Integer lastSequence) {
        this.lastSequence = lastSequence;
    }

    /**
     * Builder for TransactionSequence.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String typeCode;
        private String yearMonth;
        private Integer lastSequence;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder typeCode(String typeCode) {
            this.typeCode = typeCode;
            return this;
        }

        public Builder yearMonth(String yearMonth) {
            this.yearMonth = yearMonth;
            return this;
        }

        public Builder lastSequence(Integer lastSequence) {
            this.lastSequence = lastSequence;
            return this;
        }

        public TransactionSequence build() {
            return new TransactionSequence(id, typeCode, yearMonth, lastSequence);
        }
    }
}
