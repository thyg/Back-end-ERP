package com.rtcomops.treasury.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckbookStatsResponse {
    private int usedChecksCount;
    private BigDecimal totalAmountIssued;
    private BigDecimal totalAmountCashed;
    private int remainingChecks;
}
