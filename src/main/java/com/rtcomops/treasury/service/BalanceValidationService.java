package com.rtcomops.treasury.service;

import com.rtcomops.treasury.entity.BankAccount;
import com.rtcomops.treasury.exception.BusinessException;
import com.rtcomops.treasury.repository.BankAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service for validating account balances and overdrafts.
 * <p>
 * Provides methods to check if debit operations are authorized based on
 * current balance and overdraft limits.
 * </p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Service
public class BalanceValidationService {

    private static final Logger log = LoggerFactory.getLogger(BalanceValidationService.class);

    private final BankAccountRepository bankAccountRepository;

    public BalanceValidationService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    /**
     * Validates if a debit operation is authorized.
     *
     * @param accountId the account ID
     * @param amount the amount to debit
     * @return Mono<BalanceInfo> with validation result
     * @throws BusinessException if balance is insufficient
     */
    public Mono<BalanceInfo> validateDebitOperation(UUID accountId, BigDecimal amount) {
        return bankAccountRepository.findById(accountId)
            .switchIfEmpty(Mono.error(new BusinessException("Compte non trouve: " + accountId)))
            .map(account -> {
                BalanceInfo info = calculateBalanceInfo(account);

                if (amount != null && amount.compareTo(info.availableBalance()) > 0) {
                    log.warn("Insufficient balance for account {}: available={}, requested={}",
                        accountId, info.availableBalance(), amount);
                    throw new BusinessException(String.format(
                        "Solde insuffisant. Disponible: %s, Demande: %s",
                        info.availableBalance().toPlainString(),
                        amount.toPlainString()
                    ));
                }

                return info;
            });
    }

    /**
     * Gets balance information for an account without validation.
     *
     * @param accountId the account ID
     * @return Mono<BalanceInfo>
     */
    public Mono<BalanceInfo> getBalanceInfo(UUID accountId) {
        return bankAccountRepository.findById(accountId)
            .map(this::calculateBalanceInfo);
    }

    /**
     * Calculates balance information from an account entity.
     *
     * @param account the bank account
     * @return BalanceInfo record
     */
    public BalanceInfo calculateBalanceInfo(BankAccount account) {
        BigDecimal currentBalance = account.getCurrentBalance() != null
            ? account.getCurrentBalance()
            : BigDecimal.ZERO;

        BigDecimal overdraftLimit = Boolean.TRUE.equals(account.getOverdraftAuthorized())
            && account.getOverdraftLimit() != null
            ? account.getOverdraftLimit()
            : BigDecimal.ZERO;

        BigDecimal availableBalance = currentBalance.add(overdraftLimit);

        BigDecimal overdraftUsed = currentBalance.compareTo(BigDecimal.ZERO) < 0
            ? currentBalance.negate()
            : BigDecimal.ZERO;

        return new BalanceInfo(
            currentBalance,
            overdraftLimit,
            availableBalance,
            overdraftUsed,
            Boolean.TRUE.equals(account.getOverdraftAuthorized())
        );
    }

    /**
     * Checks if an operation would use overdraft.
     *
     * @param accountId the account ID
     * @param amount the amount to debit
     * @return Mono<Boolean> true if overdraft would be used
     */
    public Mono<Boolean> wouldUseOverdraft(UUID accountId, BigDecimal amount) {
        return bankAccountRepository.findById(accountId)
            .map(account -> {
                BigDecimal currentBalance = account.getCurrentBalance() != null
                    ? account.getCurrentBalance()
                    : BigDecimal.ZERO;
                BigDecimal projectedBalance = currentBalance.subtract(amount != null ? amount : BigDecimal.ZERO);
                return projectedBalance.compareTo(BigDecimal.ZERO) < 0;
            })
            .defaultIfEmpty(false);
    }

    /**
     * Balance information record.
     *
     * @param currentBalance the current account balance
     * @param overdraftLimit the maximum overdraft allowed
     * @param availableBalance the total available (balance + overdraft)
     * @param overdraftUsed the amount of overdraft currently used
     * @param overdraftAuthorized whether overdraft is enabled
     */
    public record BalanceInfo(
        BigDecimal currentBalance,
        BigDecimal overdraftLimit,
        BigDecimal availableBalance,
        BigDecimal overdraftUsed,
        boolean overdraftAuthorized
    ) {}
}
