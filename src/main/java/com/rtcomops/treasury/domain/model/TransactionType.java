package com.rtcomops.treasury.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model representing a transaction type category.
 *
 * <p>This is a pure POJO without any framework annotations.
 * It defines the different types of transactions (e.g., transfer, check, cash).</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
public class TransactionType {

    /**
     * Unique identifier for the transaction type.
     */
    private UUID id;

    /**
     * Unique code identifying the transaction type (e.g., "VIR", "CHQ").
     */
    private String code;

    /**
     * Human-readable label for the transaction type.
     */
    private String label;

    /**
     * Category of the transaction: BANK, CASH, CHECK, or OTHER.
     */
    private String category;

    /**
     * Optional description of the transaction type.
     */
    private String description;

    /**
     * Indicates if the transaction type is active and available for use.
     */
    private Boolean isActive;

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
    public TransactionType() {
    }

    /**
     * All-args constructor.
     */
    public TransactionType(UUID id, String code, String label, String category,
                           String description, Boolean isActive,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.code = code;
        this.label = label;
        this.category = category;
        this.description = description;
        this.isActive = isActive;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
     * Builder for TransactionType.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String code;
        private String label;
        private String category;
        private String description;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
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

        public TransactionType build() {
            return new TransactionType(id, code, label, category, description,
                                       isActive, createdAt, updatedAt);
        }
    }
}
