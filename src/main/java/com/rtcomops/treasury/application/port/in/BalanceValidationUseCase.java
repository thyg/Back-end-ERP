package com.rtcomops.treasury.application.port.in;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Input port for balance validation use cases.
 *
 * <p>Defines the contract for balance validation operations.
 * This port is implemented by the domain service.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
public interface BalanceValidationUseCase {

    /**
     * Validates if a debit operation is authorized.
     *
     * @param accountId the account ID
     * @param amount the amount to debit
     * @return Mono<BalanceInfo> with validation result
     */
    Mono<BalanceInfo> validateDebitOperation(UUID accountId, BigDecimal amount);

    /**
     * Gets balance information for an account without validation.
     *
     * @param accountId the account ID
     * @return Mono<BalanceInfo>
     */
    Mono<BalanceInfo> getBalanceInfo(UUID accountId);

    /**
     * Checks if an operation would use overdraft.
     *
     * @param accountId the account ID
     * @param amount the amount to debit
     * @return Mono<Boolean> true if overdraft would be used
     */
    Mono<Boolean> wouldUseOverdraft(UUID accountId, BigDecimal amount);

    /**
     * Balance information record.
     *
     * @param currentBalance the current account balance
     * @param overdraftLimit the maximum overdraft allowed
     * @param availableBalance the total available (balance + overdraft)
     * @param overdraftUsed the amount of overdraft currently used
     * @param overdraftAuthorized whether overdraft is enabled
     */
    record BalanceInfo(
        BigDecimal currentBalance,
        BigDecimal overdraftLimit,
        BigDecimal availableBalance,
        BigDecimal overdraftUsed,
        boolean overdraftAuthorized
    ) {}
}
