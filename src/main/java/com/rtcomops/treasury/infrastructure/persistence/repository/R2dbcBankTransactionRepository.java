package com.rtcomops.treasury.infrastructure.persistence.repository;

import com.rtcomops.treasury.infrastructure.persistence.entity.BankTransactionEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * R2DBC repository for BankTransactionEntity persistence operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Repository
public interface R2dbcBankTransactionRepository extends R2dbcRepository<BankTransactionEntity, UUID> {

    @Query("SELECT * FROM treasury.bank_transactions ORDER BY transaction_date DESC, created_at DESC")
    Flux<BankTransactionEntity> findAllOrderByDateDesc();

    @Query("SELECT * FROM treasury.bank_transactions WHERE bank_account_id = :accountId ORDER BY transaction_date DESC")
    Flux<BankTransactionEntity> findByBankAccountId(UUID accountId);

    @Query("SELECT * FROM treasury.bank_transactions WHERE bank_account_id = :accountId AND status = :status ORDER BY transaction_date DESC")
    Flux<BankTransactionEntity> findByBankAccountIdAndStatus(UUID accountId, String status);

    @Query("SELECT * FROM treasury.bank_transactions WHERE bank_account_id = :accountId AND transaction_date BETWEEN :startDate AND :endDate ORDER BY transaction_date DESC")
    Flux<BankTransactionEntity> findByBankAccountIdAndDateRange(UUID accountId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT * FROM treasury.bank_transactions WHERE bank_account_id = :accountId AND is_reconciled = false ORDER BY transaction_date ASC")
    Flux<BankTransactionEntity> findUnreconciledByAccountId(UUID accountId);

    @Query("SELECT * FROM treasury.bank_transactions WHERE status = :status ORDER BY transaction_date DESC")
    Flux<BankTransactionEntity> findByStatus(String status);

    Mono<Boolean> existsByReference(String reference);
}
