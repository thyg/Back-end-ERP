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
 * DTO for check response data.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckResponse {

    private UUID id;
    private UUID bankAccountId;
    private String bankAccountName;
    private String currency;
    private UUID checkbookId;
    private String checkbookPrefix;
    private String checkType;
    private String checkNumber;
    private BigDecimal amount;
    private String amountInWords;
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
    private String referenceCode;
    private String imageUrl;
    private String issuerBank;
    private UUID bankTransactionId;
    private UUID checkDepositId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
