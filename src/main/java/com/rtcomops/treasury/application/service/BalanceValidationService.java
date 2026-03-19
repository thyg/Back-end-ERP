package com.rtcomops.treasury.application.service;

import com.rtcomops.treasury.domain.exception.BusinessException;
import com.rtcomops.treasury.domain.model.BankAccount;
import com.rtcomops.treasury.application.port.in.BalanceValidationUseCase;
import com.rtcomops.treasury.domain.port.out.BankAccountRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Domain service for validating account balances and overdrafts.
 *
 * <p>Provides methods to check if debit operations are authorized based on
 * current balance and overdraft limits.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Service
public class BalanceValidationService implements BalanceValidationUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(BalanceValidationService.class);

    private final BankAccountRepositoryPort bankAccountRepositoryPort;

    public BalanceValidationService(BankAccountRepositoryPort bankAccountRepositoryPort) {
        this.bankAccountRepositoryPort = bankAccountRepositoryPort;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<BalanceInfo> validateDebitOperation(UUID accountId, BigDecimal amount) {
        return bankAccountRepositoryPort.findById(accountId)
            .switchIfEmpty(Mono.error(new BusinessException("Compte non trouve: " + accountId)))
            .map(account -> {
                BalanceInfo info = calculateBalanceInfo(account);

                if (amount != null && amount.compareTo(info.availableBalance()) > 0) {
                    LOG.warn("Insufficient balance for account {}: available={}, requested={}",
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
     * {@inheritDoc}
     */
    @Override
    public Mono<BalanceInfo> getBalanceInfo(UUID accountId) {
        return bankAccountRepositoryPort.findById(accountId)
            .map(this::calculateBalanceInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Boolean> wouldUseOverdraft(UUID accountId, BigDecimal amount) {
        return bankAccountRepositoryPort.findById(accountId)
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
     * Calculates balance information from a bank account domain model.
     *
     * @param account the bank account
     * @return BalanceInfo record
     */
    private BalanceInfo calculateBalanceInfo(BankAccount account) {
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
}
