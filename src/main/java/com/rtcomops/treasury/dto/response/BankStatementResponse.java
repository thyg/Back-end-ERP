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
 * DTO for bank statement response data.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankStatementResponse {

    private UUID id;
    private UUID bankAccountId;
    private String bankAccountName;
    private String reference;
    private LocalDate statementDate;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private BigDecimal totalCredits;
    private BigDecimal totalDebits;
    private Integer lineCount;
    private Integer reconciledCount;
    private BigDecimal reconciliationProgress;
    private String status;
    private String importSource;
    private String fileName;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
