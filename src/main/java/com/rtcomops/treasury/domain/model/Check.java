package com.rtcomops.treasury.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model representing a check (issued or received).
 *
 * <p>This is a pure POJO without any framework annotations.
 * It contains information about checks used in treasury operations.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
public class Check {

    private UUID id;
    private UUID bankAccountId;
    private String checkType;
    private String checkNumber;
    private BigDecimal amount;
    private String partnerName;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate depositDate;
    private LocalDate cashedDate;
    private LocalDate receiptDate;
    private LocalDate emitDate;
    private String status;
    private String description;
    private String rejectionReason;
    private UUID bankTransactionId;
    private UUID checkbookId;
    private String referenceCode;
    private String imageUrl;
    private String issuerBank;
    private UUID checkDepositId;
    private String amountInWords;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Check() {
    }

    private Check(Builder builder) {
        this.id = builder.id;
        this.bankAccountId = builder.bankAccountId;
        this.checkType = builder.checkType;
        this.checkNumber = builder.checkNumber;
        this.amount = builder.amount;
        this.partnerName = builder.partnerName;
        this.issueDate = builder.issueDate;
        this.dueDate = builder.dueDate;
        this.depositDate = builder.depositDate;
        this.cashedDate = builder.cashedDate;
        this.receiptDate = builder.receiptDate;
        this.emitDate = builder.emitDate;
        this.status = builder.status;
        this.description = builder.description;
        this.rejectionReason = builder.rejectionReason;
        this.bankTransactionId = builder.bankTransactionId;
        this.checkbookId = builder.checkbookId;
        this.referenceCode = builder.referenceCode;
        this.imageUrl = builder.imageUrl;
        this.issuerBank = builder.issuerBank;
        this.checkDepositId = builder.checkDepositId;
        this.amountInWords = builder.amountInWords;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder pre-populated with this check's values (for copying/updating).
     *
     * @return a new builder with this check's values
     */
    public Builder toBuilder() {
        return new Builder()
            .id(this.id)
            .bankAccountId(this.bankAccountId)
            .checkType(this.checkType)
            .checkNumber(this.checkNumber)
            .amount(this.amount)
            .partnerName(this.partnerName)
            .issueDate(this.issueDate)
            .dueDate(this.dueDate)
            .depositDate(this.depositDate)
            .cashedDate(this.cashedDate)
            .receiptDate(this.receiptDate)
            .emitDate(this.emitDate)
            .status(this.status)
            .description(this.description)
            .rejectionReason(this.rejectionReason)
            .bankTransactionId(this.bankTransactionId)
            .checkbookId(this.checkbookId)
            .referenceCode(this.referenceCode)
            .imageUrl(this.imageUrl)
            .issuerBank(this.issuerBank)
            .checkDepositId(this.checkDepositId)
            .amountInWords(this.amountInWords)
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt);
    }

    // =========================================================================
    // GETTERS AND SETTERS
    // =========================================================================

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

    public String getCheckType() {
        return checkType;
    }

    public void setCheckType(String checkType) {
        this.checkType = checkType;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getDepositDate() {
        return depositDate;
    }

    public void setDepositDate(LocalDate depositDate) {
        this.depositDate = depositDate;
    }

    public LocalDate getCashedDate() {
        return cashedDate;
    }

    public void setCashedDate(LocalDate cashedDate) {
        this.cashedDate = cashedDate;
    }

    public LocalDate getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(LocalDate receiptDate) {
        this.receiptDate = receiptDate;
    }

    public LocalDate getEmitDate() {
        return emitDate;
    }

    public void setEmitDate(LocalDate emitDate) {
        this.emitDate = emitDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public UUID getBankTransactionId() {
        return bankTransactionId;
    }

    public void setBankTransactionId(UUID bankTransactionId) {
        this.bankTransactionId = bankTransactionId;
    }

    public UUID getCheckbookId() {
        return checkbookId;
    }

    public void setCheckbookId(UUID checkbookId) {
        this.checkbookId = checkbookId;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getIssuerBank() {
        return issuerBank;
    }

    public void setIssuerBank(String issuerBank) {
        this.issuerBank = issuerBank;
    }

    public UUID getCheckDepositId() {
        return checkDepositId;
    }

    public void setCheckDepositId(UUID checkDepositId) {
        this.checkDepositId = checkDepositId;
    }

    public String getAmountInWords() {
        return amountInWords;
    }

    public void setAmountInWords(String amountInWords) {
        this.amountInWords = amountInWords;
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

    // =========================================================================
    // BUILDER
    // =========================================================================

    public static class Builder {
        private UUID id;
        private UUID bankAccountId;
        private String checkType;
        private String checkNumber;
        private BigDecimal amount;
        private String partnerName;
        private LocalDate issueDate;
        private LocalDate dueDate;
        private LocalDate depositDate;
        private LocalDate cashedDate;
        private LocalDate receiptDate;
        private LocalDate emitDate;
        private String status;
        private String description;
        private String rejectionReason;
        private UUID bankTransactionId;
        private UUID checkbookId;
        private String referenceCode;
        private String imageUrl;
        private String issuerBank;
        private UUID checkDepositId;
        private String amountInWords;
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

        public Builder checkType(String checkType) {
            this.checkType = checkType;
            return this;
        }

        public Builder checkNumber(String checkNumber) {
            this.checkNumber = checkNumber;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder partnerName(String partnerName) {
            this.partnerName = partnerName;
            return this;
        }

        public Builder issueDate(LocalDate issueDate) {
            this.issueDate = issueDate;
            return this;
        }

        public Builder dueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public Builder depositDate(LocalDate depositDate) {
            this.depositDate = depositDate;
            return this;
        }

        public Builder cashedDate(LocalDate cashedDate) {
            this.cashedDate = cashedDate;
            return this;
        }

        public Builder receiptDate(LocalDate receiptDate) {
            this.receiptDate = receiptDate;
            return this;
        }

        public Builder emitDate(LocalDate emitDate) {
            this.emitDate = emitDate;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder rejectionReason(String rejectionReason) {
            this.rejectionReason = rejectionReason;
            return this;
        }

        public Builder bankTransactionId(UUID bankTransactionId) {
            this.bankTransactionId = bankTransactionId;
            return this;
        }

        public Builder checkbookId(UUID checkbookId) {
            this.checkbookId = checkbookId;
            return this;
        }

        public Builder referenceCode(String referenceCode) {
            this.referenceCode = referenceCode;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder issuerBank(String issuerBank) {
            this.issuerBank = issuerBank;
            return this;
        }

        public Builder checkDepositId(UUID checkDepositId) {
            this.checkDepositId = checkDepositId;
            return this;
        }

        public Builder amountInWords(String amountInWords) {
            this.amountInWords = amountInWords;
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

        public Check build() {
            return new Check(this);
        }
    }
}
