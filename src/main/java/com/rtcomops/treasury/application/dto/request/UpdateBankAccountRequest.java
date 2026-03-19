package com.rtcomops.treasury.application.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for updating an existing bank account.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBankAccountRequest {

    private UUID bankId;

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    private Boolean isActive;
}
