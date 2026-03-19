package com.rtcomops.treasury.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for bank response data.
 *
 * <p>Used to transfer bank data to the client, hiding internal implementation details.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankResponse {

    private UUID id;
    private String code;
    private String name;
    private String swiftCode;
    private String bankCode;
    private String country;
    private String address;
    private Boolean isActive;
    private UUID bankCategoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
