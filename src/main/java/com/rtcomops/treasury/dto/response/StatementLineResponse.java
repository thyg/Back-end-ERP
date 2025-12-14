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
 * DTO for statement line response data.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatementLineResponse {

    private UUID id;
    private UUID bankStatementId;
    private Integer lineNumber;
    private LocalDate transactionDate;
    private LocalDate valueDate;
    private BigDecimal amount;
    private String direction;
    private String reference;
    private String description;
    private String partnerName;
    private String partnerAccount;
    private BigDecimal balanceAfter;
    private String reconciliationStatus;
    private LocalDateTime reconciledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
