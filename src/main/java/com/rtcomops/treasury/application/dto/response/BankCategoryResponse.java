package com.rtcomops.treasury.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for bank category.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankCategoryResponse {
    private UUID id;
    private String code;
    private String label;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
