package com.rtcomops.treasury.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for transaction type response data.
 *
 * <p>Used to transfer transaction type data to the client.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionTypeResponse {

    private UUID id;
    private String code;
    private String label;
    private String direction;
    private String category;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
