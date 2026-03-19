package com.rtcomops.treasury.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Domain model representing a company bank account.
 *
 * <p>This is a pure domain object (POJO) without any framework annotations.
 * It contains business logic related to bank account operations.</p>
 *
 * <p>Note: The 'details' field is stored as Map<String, Object> in the domain model,
 * while it's stored as JSON in the database. The persistence mapper handles the conversion.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
public class BankAccount {

    private UUID id;
    private UUID bankId;
    private UUID accountTypeId;
    private UUID accountSubTypeId;
    private UUID connectorTypeId;
    private String name;
    private String branchCode;
    private String generatedIban;
    private String currency;
    private BigDecimal currentBalance;
    private BigDecimal reconciledBalance;
    private Boolean isActive;
    private Map<String, Object> details;
    private Boolean overdraftAuthorized;
    private BigDecimal overdraftLimit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BankAccount() {
    }

    private BankAccount(Builder builder) {
        this.id = builder.id;
        this.bankId = builder.bankId;
        this.accountTypeId = builder.accountTypeId;
        this.accountSubTypeId = builder.accountSubTypeId;
        this.connectorTypeId = builder.connectorTypeId;
        this.name = builder.name;
        this.branchCode = builder.branchCode;
        this.generatedIban = builder.generatedIban;
        this.currency = builder.currency;
        this.currentBalance = builder.currentBalance;
        this.reconciledBalance = builder.reconciledBalance;
        this.isActive = builder.isActive;
        this.details = builder.details;
        this.overdraftAuthorized = builder.overdraftAuthorized;
        this.overdraftLimit = builder.overdraftLimit;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    // =========================================================================
    // OVERDRAFT UTILITY METHODS
    // =========================================================================

    /**
     * Calculates the available balance including overdraft.
     *
     * @return available balance (current + overdraft limit)
     */
    public BigDecimal getAvailableBalance() {
        BigDecimal balance = currentBalance != null ? currentBalance : BigDecimal.ZERO;
        BigDecimal limit = (Boolean.TRUE.equals(overdraftAuthorized) && overdraftLimit != null)
            ? overdraftLimit
            : BigDecimal.ZERO;
        return balance.add(limit);
    }

    /**
     * Calculates the currently used overdraft amount.
     *
     * @return overdraft used (0 if balance is positive)
     */
    public BigDecimal getOverdraftUsed() {
        if (currentBalance == null || currentBalance.compareTo(BigDecimal.ZERO) >= 0) {
            return BigDecimal.ZERO;
        }
        return currentBalance.negate();
    }

    /**
     * Checks if an operation of the given amount is authorized.
     *
     * @param amount the amount to debit
     * @return true if the operation is authorized
     */
    public boolean isOperationAuthorized(BigDecimal amount) {
        if (amount == null) {
            return true;
        }
        return amount.compareTo(getAvailableBalance()) <= 0;
    }

    /**
     * Calculates the remaining overdraft capacity.
     *
     * @return remaining overdraft (overdraft limit - overdraft used)
     */
    public BigDecimal getRemainingOverdraft() {
        if (!Boolean.TRUE.equals(overdraftAuthorized) || overdraftLimit == null) {
            return BigDecimal.ZERO;
        }
        return overdraftLimit.subtract(getOverdraftUsed()).max(BigDecimal.ZERO);
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

    public UUID getBankId() {
        return bankId;
    }

    public void setBankId(UUID bankId) {
        this.bankId = bankId;
    }

    public UUID getAccountTypeId() {
        return accountTypeId;
    }

    public void setAccountTypeId(UUID accountTypeId) {
        this.accountTypeId = accountTypeId;
    }

    public UUID getAccountSubTypeId() {
        return accountSubTypeId;
    }

    public void setAccountSubTypeId(UUID accountSubTypeId) {
        this.accountSubTypeId = accountSubTypeId;
    }

    public UUID getConnectorTypeId() {
        return connectorTypeId;
    }

    public void setConnectorTypeId(UUID connectorTypeId) {
        this.connectorTypeId = connectorTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getGeneratedIban() {
        return generatedIban;
    }

    public void setGeneratedIban(String generatedIban) {
        this.generatedIban = generatedIban;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public BigDecimal getReconciledBalance() {
        return reconciledBalance;
    }

    public void setReconciledBalance(BigDecimal reconciledBalance) {
        this.reconciledBalance = reconciledBalance;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public Boolean getOverdraftAuthorized() {
        return overdraftAuthorized;
    }

    public void setOverdraftAuthorized(Boolean overdraftAuthorized) {
        this.overdraftAuthorized = overdraftAuthorized;
    }

    public BigDecimal getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(BigDecimal overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
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
        private UUID bankId;
        private UUID accountTypeId;
        private UUID accountSubTypeId;
        private UUID connectorTypeId;
        private String name;
        private String branchCode;
        private String generatedIban;
        private String currency;
        private BigDecimal currentBalance;
        private BigDecimal reconciledBalance;
        private Boolean isActive;
        private Map<String, Object> details;
        private Boolean overdraftAuthorized;
        private BigDecimal overdraftLimit;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder bankId(UUID bankId) {
            this.bankId = bankId;
            return this;
        }

        public Builder accountTypeId(UUID accountTypeId) {
            this.accountTypeId = accountTypeId;
            return this;
        }

        public Builder accountSubTypeId(UUID accountSubTypeId) {
            this.accountSubTypeId = accountSubTypeId;
            return this;
        }

        public Builder connectorTypeId(UUID connectorTypeId) {
            this.connectorTypeId = connectorTypeId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder branchCode(String branchCode) {
            this.branchCode = branchCode;
            return this;
        }

        public Builder generatedIban(String generatedIban) {
            this.generatedIban = generatedIban;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder currentBalance(BigDecimal currentBalance) {
            this.currentBalance = currentBalance;
            return this;
        }

        public Builder reconciledBalance(BigDecimal reconciledBalance) {
            this.reconciledBalance = reconciledBalance;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder details(Map<String, Object> details) {
            this.details = details;
            return this;
        }

        public Builder overdraftAuthorized(Boolean overdraftAuthorized) {
            this.overdraftAuthorized = overdraftAuthorized;
            return this;
        }

        public Builder overdraftLimit(BigDecimal overdraftLimit) {
            this.overdraftLimit = overdraftLimit;
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

        public BankAccount build() {
            return new BankAccount(this);
        }
    }
}
