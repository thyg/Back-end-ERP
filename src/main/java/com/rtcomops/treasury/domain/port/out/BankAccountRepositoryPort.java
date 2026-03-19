package com.rtcomops.treasury.domain.port.out;

import com.rtcomops.treasury.domain.model.BankAccount;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Output port for BankAccount repository operations.
 *
 * <p>Defines the contract for persistence operations on BankAccount entities.
 * This port is implemented by the infrastructure layer adapter.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
public interface BankAccountRepositoryPort {

    /**
     * Finds a bank account by its unique identifier.
     *
     * @param id the bank account ID
     * @return Mono containing the bank account if found, empty otherwise
     */
    Mono<BankAccount> findById(UUID id);

    /**
     * Finds all active bank accounts ordered by name.
     *
     * @return Flux of active bank accounts
     */
    Flux<BankAccount> findAllActiveOrderByName();

    /**
     * Finds all bank accounts ordered by name.
     *
     * @return Flux of all bank accounts
     */
    Flux<BankAccount> findAllOrderByName();

    /**
     * Finds all bank accounts belonging to a specific bank.
     *
     * @param bankId the bank ID
     * @return Flux of bank accounts for the specified bank
     */
    Flux<BankAccount> findByBankId(UUID bankId);

    /**
     * Saves a bank account (insert or update).
     *
     * @param bankAccount the bank account to save
     * @return Mono containing the saved bank account
     */
    Mono<BankAccount> save(BankAccount bankAccount);

    /**
     * Deletes a bank account.
     *
     * @param bankAccount the bank account to delete
     * @return Mono that completes when deletion is done
     */
    Mono<Void> delete(BankAccount bankAccount);
}
