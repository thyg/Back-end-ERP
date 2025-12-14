package com.rtcomops.treasury.repository;

import com.rtcomops.treasury.entity.BankTransaction;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Reactive repository for BankTransaction entity operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Repository
public interface BankTransactionRepository extends R2dbcRepository<BankTransaction, UUID> {

    @Query("SELECT * FROM treasury.bank_transactions ORDER BY transaction_date DESC, created_at DESC")
    Flux<BankTransaction> findAllOrderByDateDesc();

    @Query("SELECT * FROM treasury.bank_transactions WHERE bank_account_id = :accountId ORDER BY transaction_date DESC")
    Flux<BankTransaction> findByBankAccountId(UUID accountId);

    @Query("SELECT * FROM treasury.bank_transactions WHERE bank_account_id = :accountId AND status = :status ORDER BY transaction_date DESC")
    Flux<BankTransaction> findByBankAccountIdAndStatus(UUID accountId, String status);

    @Query("SELECT * FROM treasury.bank_transactions WHERE bank_account_id = :accountId AND transaction_date BETWEEN :startDate AND :endDate ORDER BY transaction_date DESC")
    Flux<BankTransaction> findByBankAccountIdAndDateRange(UUID accountId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT * FROM treasury.bank_transactions WHERE bank_account_id = :accountId AND is_reconciled = false ORDER BY transaction_date ASC")
    Flux<BankTransaction> findUnreconciledByAccountId(UUID accountId);

    @Query("SELECT * FROM treasury.bank_transactions WHERE status = :status ORDER BY transaction_date DESC")
    Flux<BankTransaction> findByStatus(String status);

    Mono<Boolean> existsByReference(String reference);
}
