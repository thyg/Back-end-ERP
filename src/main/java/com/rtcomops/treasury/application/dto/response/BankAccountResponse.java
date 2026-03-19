package com.rtcomops.treasury.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO response for bank account operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountResponse {

    private UUID id;
    private UUID bankId;
    private String bankName;
    private String name;
    private String branchCode;
    private String accountNumber;
    private String generatedIban;
    private String iban;
    private String bic;

    private String currency;
    private BigDecimal currentBalance;
    private BigDecimal reconciledBalance;
    private Boolean isActive;

    private Map<String, Object> details;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
