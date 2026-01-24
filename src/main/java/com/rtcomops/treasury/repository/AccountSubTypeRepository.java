package com.rtcomops.treasury.repository;

import com.rtcomops.treasury.entity.AccountSubType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive repository for AccountSubType entity operations.
 *
 * <p>Provides non-blocking CRUD operations and custom queries
 * for the treasury.account_sub_types table using R2DBC.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Repository
public interface AccountSubTypeRepository extends R2dbcRepository<AccountSubType, UUID> {

    /**
     * Finds all sub-types for a given account type.
     *
     * @param accountTypeId the parent account type ID
     * @return Flux of sub-types belonging to the account type
     */
    @Query("SELECT * FROM treasury.account_sub_types WHERE account_type_id = :accountTypeId ORDER BY ordre_affichage ASC, code ASC")
    Flux<AccountSubType> findByAccountTypeId(UUID accountTypeId);

    /**
     * Finds all active sub-types for a given account type.
     *
     * @param accountTypeId the parent account type ID
     * @return Flux of active sub-types belonging to the account type
     */
    @Query("SELECT * FROM treasury.account_sub_types WHERE account_type_id = :accountTypeId AND is_active = true ORDER BY ordre_affichage ASC, code ASC")
    Flux<AccountSubType> findByAccountTypeIdAndIsActiveTrue(UUID accountTypeId);

    /**
     * Finds a sub-type by its code within a parent account type.
     *
     * @param accountTypeId the parent account type ID
     * @param code the sub-type code
     * @return Mono containing the sub-type if found, empty otherwise
     */
    @Query("SELECT * FROM treasury.account_sub_types WHERE account_type_id = :accountTypeId AND code = :code")
    Mono<AccountSubType> findByAccountTypeIdAndCode(UUID accountTypeId, String code);

    /**
     * Checks if a sub-type with the given code exists within a parent type.
     *
     * @param accountTypeId the parent account type ID
     * @param code the code to check
     * @return Mono containing true if exists, false otherwise
     */
    @Query("SELECT COUNT(*) > 0 FROM treasury.account_sub_types WHERE account_type_id = :accountTypeId AND code = :code")
    Mono<Boolean> existsByAccountTypeIdAndCode(UUID accountTypeId, String code);

    /**
     * Checks if a sub-type with the given code exists within a parent type, excluding a specific ID.
     *
     * @param accountTypeId the parent account type ID
     * @param code the code to check
     * @param id the ID to exclude from the check
     * @return Mono containing true if another sub-type has this code
     */
    @Query("SELECT COUNT(*) > 0 FROM treasury.account_sub_types WHERE account_type_id = :accountTypeId AND code = :code AND id != :id")
    Mono<Boolean> existsByAccountTypeIdAndCodeAndIdNot(UUID accountTypeId, String code, UUID id);

    /**
     * Finds all active sub-types ordered by display order.
     *
     * @return Flux of all active sub-types
     */
    @Query("SELECT * FROM treasury.account_sub_types WHERE is_active = true ORDER BY ordre_affichage ASC, code ASC")
    Flux<AccountSubType> findAllActiveOrderByOrdre();

    /**
     * Finds all sub-types ordered by display order.
     *
     * @return Flux of all sub-types
     */
    @Query("SELECT * FROM treasury.account_sub_types ORDER BY ordre_affichage ASC, code ASC")
    Flux<AccountSubType> findAllOrderByOrdre();

    /**
     * Counts sub-types for a given account type.
     *
     * @param accountTypeId the parent account type ID
     * @return Mono containing the count
     */
    @Query("SELECT COUNT(*) FROM treasury.account_sub_types WHERE account_type_id = :accountTypeId")
    Mono<Long> countByAccountTypeId(UUID accountTypeId);

    /**
     * Deletes all sub-types for a given account type.
     *
     * @param accountTypeId the parent account type ID
     * @return Mono signaling completion
     */
    @Query("DELETE FROM treasury.account_sub_types WHERE account_type_id = :accountTypeId")
    Mono<Void> deleteByAccountTypeId(UUID accountTypeId);
}
