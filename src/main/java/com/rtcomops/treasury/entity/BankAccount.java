package com.rtcomops.treasury.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import io.r2dbc.postgresql.codec.Json;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a company bank account.
 *
 * <p>This entity maps to the treasury.bank_accounts table and contains
 * information about bank accounts used for treasury operations.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "treasury", name = "bank_accounts")
public class BankAccount implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("bank_id")
    private UUID bankId;

    /**
     * Reference to the account type configuration.
     */
    @Column("account_type_id")
    private UUID accountTypeId;

    /**
     * Reference to the account sub-type configuration (optional).
     */
    @Column("account_sub_type_id")
    private UUID accountSubTypeId;

    @Column("name")
    private String name;

  //  @Column("account_number")
   // private String accountNumber;

    /**
     * Branch/agency code (5 digits) for IBAN generation.
     */
    @Column("branch_code")
    private String branchCode;

    /**
     * Generated IBAN based on bank code, branch code, and account number.
     */
    @Column("generated_iban")
    private String generatedIban;

   // @Column("iban")
   // private String iban;

    //@Column("bic")
    //private String bic;

    @Column("currency")
    private String currency;

    @Column("current_balance")
    private BigDecimal currentBalance;

    @Column("reconciled_balance")
    private BigDecimal reconciledBalance;

    @Column("is_active")
    private Boolean isActive;

    @Column("connector_type_id")
    private UUID connectorTypeId;
    
    @Column("details")
    private Json details; 

    /**
     * Indicates if overdraft is authorized for this account.
     */
    @Column("overdraft_authorized")
    private Boolean overdraftAuthorized;

    /**
     * Maximum overdraft limit in account currency.
     */
    @Column("overdraft_limit")
    private BigDecimal overdraftLimit;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

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

    public BankAccount markNotNew() {
        this.isNew = false;
        return this;
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
}
