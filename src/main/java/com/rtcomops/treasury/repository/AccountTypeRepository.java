package com.rtcomops.treasury.repository;

import com.rtcomops.treasury.entity.AccountType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive repository for AccountType entity operations.
 *
 * <p>Provides non-blocking CRUD operations and custom queries
 * for the treasury.account_types table using R2DBC.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Repository
public interface AccountTypeRepository extends R2dbcRepository<AccountType, UUID> {

    /**
     * Finds an account type by its unique code.
     *
     * @param code the account type code to search for
     * @return Mono containing the account type if found, empty otherwise
     */
    Mono<AccountType> findByCode(String code);

    /**
     * Checks if an account type with the given code exists.
     *
     * @param code the code to check
     * @return Mono containing true if exists, false otherwise
     */
    Mono<Boolean> existsByCode(String code);

    /**
     * Finds all active account types ordered by display order.
     *
     * @return Flux of active account types
     */
    @Query("SELECT * FROM treasury.account_types WHERE is_active = true ORDER BY ordre_affichage ASC, code ASC")
    Flux<AccountType> findAllActiveOrderByOrdre();

    /**
     * Finds all account types ordered by display order.
     *
     * @return Flux of all account types
     */
    @Query("SELECT * FROM treasury.account_types ORDER BY ordre_affichage ASC, code ASC")
    Flux<AccountType> findAllOrderByOrdre();

    /**
     * Finds account types that allow check emission.
     *
     * @return Flux of account types that can emit checks
     */
    @Query("SELECT * FROM treasury.account_types WHERE peut_emettre_cheques = true AND is_active = true ORDER BY ordre_affichage ASC")
    Flux<AccountType> findByPeutEmettreChequesTrue();

    /**
     * Finds account types that allow check reception.
     *
     * @return Flux of account types that can receive checks
     */
    @Query("SELECT * FROM treasury.account_types WHERE peut_recevoir_cheques = true AND is_active = true ORDER BY ordre_affichage ASC")
    Flux<AccountType> findByPeutRecevoirChequesTrue();

    /**
     * Finds account types that allow cash transactions.
     *
     * @return Flux of account types that allow cash
     */
    @Query("SELECT * FROM treasury.account_types WHERE peut_transactions_especes = true AND is_active = true ORDER BY ordre_affichage ASC")
    Flux<AccountType> findByPeutTransactionsEspecesTrue();

    /**
     * Finds account types that allow overdraft.
     *
     * @return Flux of account types with overdraft authorized
     */
    @Query("SELECT * FROM treasury.account_types WHERE decouvert_autorise = true AND is_active = true ORDER BY ordre_affichage ASC")
    Flux<AccountType> findByDecouvertAutoriseTrue();

    /**
     * Checks if an account type with the given code exists, excluding a specific ID.
     *
     * @param code the code to check
     * @param id the ID to exclude from the check
     * @return Mono containing true if another account type has this code
     */
    @Query("SELECT COUNT(*) > 0 FROM treasury.account_types WHERE code = :code AND id != :id")
    Mono<Boolean> existsByCodeAndIdNot(String code, UUID id);
}
