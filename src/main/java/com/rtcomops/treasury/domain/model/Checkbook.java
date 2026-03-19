package com.rtcomops.treasury.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model representing a checkbook (chéquier).
 *
 * <p>This is a pure POJO without any framework annotations.
 * It manages ranges of check numbers associated with a bank account.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
public class Checkbook {

    private UUID id;
    private UUID bankAccountId;
    private String iban;
    private String prefix;
    private Integer startNumber;
    private Integer endNumber;
    private Integer numberOfPages;
    private Integer currentNumber;
    private String status;
    private String type = "REEL";
    private Boolean isSystem = false;
    private Long nextSequence;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Checkbook() {
    }

    private Checkbook(Builder builder) {
        this.id = builder.id;
        this.bankAccountId = builder.bankAccountId;
        this.iban = builder.iban;
        this.prefix = builder.prefix;
        this.startNumber = builder.startNumber;
        this.endNumber = builder.endNumber;
        this.numberOfPages = builder.numberOfPages;
        this.currentNumber = builder.currentNumber;
        this.status = builder.status;
        this.type = builder.type;
        this.isSystem = builder.isSystem;
        this.nextSequence = builder.nextSequence;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getBankAccountId() { return bankAccountId; }
    public void setBankAccountId(UUID bankAccountId) { this.bankAccountId = bankAccountId; }

    public String getIban() { return iban; }
    public void setIban(String iban) { this.iban = iban; }

    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }

    public Integer getStartNumber() { return startNumber; }
    public void setStartNumber(Integer startNumber) { this.startNumber = startNumber; }

    public Integer getEndNumber() { return endNumber; }
    public void setEndNumber(Integer endNumber) { this.endNumber = endNumber; }

    public Integer getNumberOfPages() { return numberOfPages; }
    public void setNumberOfPages(Integer numberOfPages) { this.numberOfPages = numberOfPages; }

    public Integer getCurrentNumber() { return currentNumber; }
    public void setCurrentNumber(Integer currentNumber) { this.currentNumber = currentNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Boolean getIsSystem() { return isSystem; }
    public void setIsSystem(Boolean isSystem) { this.isSystem = isSystem; }

    public Long getNextSequence() { return nextSequence; }
    public void setNextSequence(Long nextSequence) { this.nextSequence = nextSequence; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Checks if this is the fictif system checkbook.
     *
     * @return true if this is a fictif checkbook
     */
    public boolean isFictif() {
        return "FICTIF".equals(type);
    }

    /**
     * Checks if this is a real (physical) checkbook.
     *
     * @return true if this is a real checkbook
     */
    public boolean isReel() {
        return "REEL".equals(type);
    }

    /**
     * Gets the next check number formatted with the prefix.
     *
     * @return the formatted check number (e.g., "4587000001")
     */
    public String getNextCheckNumber() {
        if (currentNumber == null || prefix == null) {
            return null;
        }
        return prefix + String.format("%06d", currentNumber);
    }

    /**
     * Checks if the checkbook has available checks.
     * Fictif checkbooks always have unlimited checks.
     *
     * @return true if checks are available and status is ACTIVE
     */
    public boolean hasAvailableChecks() {
        if (isFictif()) {
            return "ACTIVE".equals(status);
        }
        return currentNumber != null
            && endNumber != null
            && currentNumber <= endNumber
            && "ACTIVE".equals(status);
    }

    /**
     * Calculates the number of remaining checks.
     * Returns -1 for fictif checkbooks (unlimited).
     *
     * @return the number of available checks, -1 for unlimited (fictif), or 0 if none
     */
    public int getAvailableChecksCount() {
        if (isFictif()) {
            return -1; // Unlimited
        }
        if (currentNumber == null || endNumber == null || currentNumber > endNumber) {
            return 0;
        }
        return endNumber - currentNumber + 1;
    }

    /**
     * Generates the next check number for received checks (fictif checkbook).
     * Format: CHQ-REC-000001, CHQ-REC-000002, etc.
     *
     * @return formatted check number for received checks
     */
    public String getNextReceivedCheckNumber() {
        if (!isFictif() || nextSequence == null) {
            return null;
        }
        return "CHQ-REC-" + String.format("%06d", nextSequence);
    }

    public static class Builder {
        private UUID id;
        private UUID bankAccountId;
        private String iban;
        private String prefix;
        private Integer startNumber;
        private Integer endNumber;
        private Integer numberOfPages;
        private Integer currentNumber;
        private String status;
        private String type = "REEL";
        private Boolean isSystem = false;
        private Long nextSequence;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder bankAccountId(UUID bankAccountId) { this.bankAccountId = bankAccountId; return this; }
        public Builder iban(String iban) { this.iban = iban; return this; }
        public Builder prefix(String prefix) { this.prefix = prefix; return this; }
        public Builder startNumber(Integer startNumber) { this.startNumber = startNumber; return this; }
        public Builder endNumber(Integer endNumber) { this.endNumber = endNumber; return this; }
        public Builder numberOfPages(Integer numberOfPages) { this.numberOfPages = numberOfPages; return this; }
        public Builder currentNumber(Integer currentNumber) { this.currentNumber = currentNumber; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder type(String type) { this.type = type; return this; }
        public Builder isSystem(Boolean isSystem) { this.isSystem = isSystem; return this; }
        public Builder nextSequence(Long nextSequence) { this.nextSequence = nextSequence; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Checkbook build() {
            return new Checkbook(this);
        }
    }
}
