package com.rtcomops.treasury.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model representing a manual bank transaction.
 *
 * <p>This is a pure POJO without any framework annotations.
 * It contains information about individual bank transactions.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
public class BankTransaction {

    /**
     * Unique identifier for the transaction.
     */
    private UUID id;

    /**
     * ID of the bank account associated with this transaction.
     */
    private UUID bankAccountId;

    /**
     * ID of the transaction type.
     */
    private UUID transactionTypeId;

    /**
     * Unique reference for the transaction (auto-generated).
     */
    private String reference;

    /**
     * Date of the transaction.
     */
    private LocalDate transactionDate;

    /**
     * Value date of the transaction (when it affects the balance).
     */
    private LocalDate valueDate;

    /**
     * Amount of the transaction.
     */
    private BigDecimal amount;

    /**
     * Direction of the transaction: CREDIT or DEBIT.
     */
    private String direction;

    /**
     * Description of the transaction.
     */
    private String description;

    /**
     * Name of the partner/counterparty.
     */
    private String partnerName;

    /**
     * Status of the transaction: DRAFT, VALIDATED, or CANCELLED.
     */
    private String status;

    /**
     * System date when the transaction was created (for display purposes).
     */
    private LocalDateTime systemDate;

    /**
     * Indicates if the transaction has been reconciled.
     */
    private Boolean isReconciled;

    /**
     * Timestamp when the transaction was reconciled.
     */
    private LocalDateTime reconciledAt;

    /**
     * ID of the statement line this transaction is reconciled with.
     */
    private UUID statementLineId;

    /**
     * Timestamp when the record was created.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the record was last updated.
     */
    private LocalDateTime updatedAt;

    /**
     * Default constructor.
     */
    public BankTransaction() {
    }

    /**
     * All-args constructor.
     */
    public BankTransaction(UUID id, UUID bankAccountId, UUID transactionTypeId, String reference,
                           LocalDate transactionDate, LocalDate valueDate, BigDecimal amount,
                           String direction, String description, String partnerName, String status,
                           LocalDateTime systemDate, Boolean isReconciled, LocalDateTime reconciledAt,
                           UUID statementLineId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.bankAccountId = bankAccountId;
        this.transactionTypeId = transactionTypeId;
        this.reference = reference;
        this.transactionDate = transactionDate;
        this.valueDate = valueDate;
        this.amount = amount;
        this.direction = direction;
        this.description = description;
        this.partnerName = partnerName;
        this.status = status;
        this.systemDate = systemDate;
        this.isReconciled = isReconciled;
        this.reconciledAt = reconciledAt;
        this.statementLineId = statementLineId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBankAccountId() {
        return bankAccountId;
    }

    public void setBankAccountId(UUID bankAccountId) {
        this.bankAccountId = bankAccountId;
    }

    public UUID getTransactionTypeId() {
        return transactionTypeId;
    }

    public void setTransactionTypeId(UUID transactionTypeId) {
        this.transactionTypeId = transactionTypeId;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public LocalDate getValueDate() {
        return valueDate;
    }

    public void setValueDate(LocalDate valueDate) {
        this.valueDate = valueDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSystemDate() {
        return systemDate;
    }

    public void setSystemDate(LocalDateTime systemDate) {
        this.systemDate = systemDate;
    }

    public Boolean getIsReconciled() {
        return isReconciled;
    }

    public void setIsReconciled(Boolean isReconciled) {
        this.isReconciled = isReconciled;
    }

    public LocalDateTime getReconciledAt() {
        return reconciledAt;
    }

    public void setReconciledAt(LocalDateTime reconciledAt) {
        this.reconciledAt = reconciledAt;
    }

    public UUID getStatementLineId() {
        return statementLineId;
    }

    public void setStatementLineId(UUID statementLineId) {
        this.statementLineId = statementLineId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Builder for BankTransaction.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID bankAccountId;
        private UUID transactionTypeId;
        private String reference;
        private LocalDate transactionDate;
        private LocalDate valueDate;
        private BigDecimal amount;
        private String direction;
        private String description;
        private String partnerName;
        private String status;
        private LocalDateTime systemDate;
        private Boolean isReconciled;
        private LocalDateTime reconciledAt;
        private UUID statementLineId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder bankAccountId(UUID bankAccountId) {
            this.bankAccountId = bankAccountId;
            return this;
        }

        public Builder transactionTypeId(UUID transactionTypeId) {
            this.transactionTypeId = transactionTypeId;
            return this;
        }

        public Builder reference(String reference) {
            this.reference = reference;
            return this;
        }

        public Builder transactionDate(LocalDate transactionDate) {
            this.transactionDate = transactionDate;
            return this;
        }

        public Builder valueDate(LocalDate valueDate) {
            this.valueDate = valueDate;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder direction(String direction) {
            this.direction = direction;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder partnerName(String partnerName) {
            this.partnerName = partnerName;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder systemDate(LocalDateTime systemDate) {
            this.systemDate = systemDate;
            return this;
        }

        public Builder isReconciled(Boolean isReconciled) {
            this.isReconciled = isReconciled;
            return this;
        }

        public Builder reconciledAt(LocalDateTime reconciledAt) {
            this.reconciledAt = reconciledAt;
            return this;
        }

        public Builder statementLineId(UUID statementLineId) {
            this.statementLineId = statementLineId;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public BankTransaction build() {
            return new BankTransaction(id, bankAccountId, transactionTypeId, reference,
                    transactionDate, valueDate, amount, direction, description, partnerName,
                    status, systemDate, isReconciled, reconciledAt, statementLineId,
                    createdAt, updatedAt);
        }
    }
}
