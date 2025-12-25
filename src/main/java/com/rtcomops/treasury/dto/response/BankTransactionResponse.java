package com.rtcomops.treasury.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for bank transaction response data.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankTransactionResponse {

    private UUID id;
    private UUID bankAccountId;
    private String bankAccountName;
    private UUID transactionTypeId;
    private String transactionTypeCode;
    private String transactionTypeLabel;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
