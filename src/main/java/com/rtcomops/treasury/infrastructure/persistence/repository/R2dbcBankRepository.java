package com.rtcomops.treasury.infrastructure.persistence.repository;

import com.rtcomops.treasury.infrastructure.persistence.entity.BankEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Spring Data R2DBC repository for BankEntity operations.
 *
 * <p>Provides non-blocking CRUD operations and custom queries
 * for the treasury.banks table using R2DBC.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@Repository
public interface R2dbcBankRepository extends R2dbcRepository<BankEntity, UUID> {

    /**
     * Finds a bank by its unique code.
     *
     * @param code the bank code to search for
     * @return Mono containing the bank if found, empty otherwise
     */
    Mono<BankEntity> findByCode(String code);

    /**
     * Checks if a bank with the given code exists.
     *
     * @param code the bank code to check
     * @return Mono containing true if exists, false otherwise
     */
    Mono<Boolean> existsByCode(String code);

    /**
     * Finds all active banks ordered by name.
     *
     * @return Flux of active banks
     */
    @Query("SELECT * FROM treasury.banks WHERE is_active = true ORDER BY name ASC")
    Flux<BankEntity> findAllActiveOrderByName();

    /**
     * Finds all banks ordered by name.
     *
     * @return Flux of all banks
     */
    @Query("SELECT * FROM treasury.banks ORDER BY name ASC")
    Flux<BankEntity> findAllOrderByName();

    /**
     * Checks if a bank with the given code exists, excluding a specific ID.
     * Used for update validation to prevent duplicate codes.
     *
     * @param code the bank code to check
     * @param id the ID to exclude from the check
     * @return Mono containing true if another bank has this code
     */
    @Query("SELECT COUNT(*) > 0 FROM treasury.banks WHERE code = :code AND id != :id")
    Mono<Boolean> existsByCodeAndIdNot(String code, UUID id);
}
