package com.rtcomops.treasury.application.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO for creating a new check deposit batch (remise de cheques en lot).
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2026-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCheckDepositRequest {

    /**
     * List of check IDs to include in the deposit batch.
     * All checks must have status RECEIVED and belong to the same bank account.
     */
    @NotEmpty(message = "At least one check ID is required")
    private List<UUID> checkIds;

    /**
     * Bank account where the checks will be deposited.
     * Must match the bank account of all provided checks.
     */
    @NotNull(message = "Bank account ID is required")
    private UUID bankAccountId;

    /**
     * Date of the deposit.
     * Must not be earlier than the most recent receiptDate among the selected checks.
     */
    @NotNull(message = "Deposit date is required")
    private LocalDate depositDate;
}
