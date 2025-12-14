package com.rtcomops.treasury.repository;

import com.rtcomops.treasury.entity.BankAccount;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive repository for BankAccount entity operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Repository
public interface BankAccountRepository extends R2dbcRepository<BankAccount, UUID> {

    @Query("SELECT * FROM treasury.bank_accounts WHERE is_active = true ORDER BY name ASC")
    Flux<BankAccount> findAllActiveOrderByName();

    @Query("SELECT * FROM treasury.bank_accounts ORDER BY name ASC")
    Flux<BankAccount> findAllOrderByName();

    @Query("SELECT * FROM treasury.bank_accounts WHERE bank_id = :bankId ORDER BY name ASC")
    Flux<BankAccount> findByBankId(UUID bankId);

    Mono<Boolean> existsByAccountNumber(String accountNumber);

    @Query("SELECT COUNT(*) > 0 FROM treasury.bank_accounts WHERE account_number = :accountNumber AND id != :id")
    Mono<Boolean> existsByAccountNumberAndIdNot(String accountNumber, UUID id);

    Mono<Boolean> existsByIban(String iban);

    @Query("SELECT COUNT(*) > 0 FROM treasury.bank_accounts WHERE iban = :iban AND id != :id")
    Mono<Boolean> existsByIbanAndIdNot(String iban, UUID id);
}
