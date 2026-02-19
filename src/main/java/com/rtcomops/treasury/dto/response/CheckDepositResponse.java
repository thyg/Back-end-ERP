package com.rtcomops.treasury.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for check deposit batch (remise de cheques en lot) response data.
 *
 * @author RT-ComOps Team
 * @version 1.1.0
 * @since 2026-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckDepositResponse {

    private UUID id;

    /**
     * Unique reference for the deposit batch (e.g., REM-202602-0001).
     */
    private String reference;

    /**
     * Date when the checks were deposited at the bank.
     */
    private LocalDate depositDate;

    /**
     * Bank account receiving the deposit.
     */
    private UUID bankAccountId;

    /**
     * Name of the bank account.
     */
    private String bankAccountName;

    /**
     * Currency of the bank account.
     */
    private String currency;

    /**
     * Sum of all check amounts in the batch.
     */
    private BigDecimal totalAmount;

    /**
     * Number of checks in the batch.
     */
    private Integer checkCount;

    /**
     * Current status of the deposit.
     * PENDING = created, awaiting bank deposit; DEPOSITED = at bank, awaiting cash;
     * CASHED = funds received; RECONCILED = matched with bank statement.
     */
    private String status;

    /**
     * Date when the deposit was cashed (funds received).
     */
    private LocalDate cashedDate;

    /**
     * Link to the unique bank transaction created upon cashing.
     */
    private UUID bankTransactionId;

    /**
     * Reference of the linked bank transaction (if reconciled).
     */
    private String bankTransactionReference;

    /**
     * List of checks included in this deposit batch.
     */
    private List<CheckResponse> checks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
