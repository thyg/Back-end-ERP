package com.rtcomops.treasury.infrastructure.persistence.entity;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Persistence entity for Check.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "treasury", name = "checks")
public class CheckEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("bank_account_id")
    private UUID bankAccountId;

    @Column("check_type")
    private String checkType;

    @Column("check_number")
    private String checkNumber;

    @Column("amount")
    private BigDecimal amount;

    @Column("partner_name")
    private String partnerName;

    @Column("issue_date")
    private LocalDate issueDate;

    @Column("due_date")
    private LocalDate dueDate;

    @Column("deposit_date")
    private LocalDate depositDate;

    @Column("cashed_date")
    private LocalDate cashedDate;

    @Column("receipt_date")
    private LocalDate receiptDate;

    @Column("emit_date")
    private LocalDate emitDate;

    @Column("status")
    private String status;

    @Column("description")
    private String description;

    @Column("rejection_reason")
    private String rejectionReason;

    @Column("bank_transaction_id")
    private UUID bankTransactionId;

    @Column("checkbook_id")
    private UUID checkbookId;

    @Column("reference_code")
    private String referenceCode;

    @Column("image_url")
    private String imageUrl;

    @Column("issuer_bank")
    private String issuerBank;

    @Column("check_deposit_id")
    private UUID checkDepositId;

    @Column("amount_in_words")
    private String amountInWords;

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

    public CheckEntity markNotNew() {
        this.isNew = false;
        return this;
    }
}
