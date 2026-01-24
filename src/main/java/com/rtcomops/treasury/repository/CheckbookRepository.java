package com.rtcomops.treasury.repository;

import com.rtcomops.treasury.entity.Checkbook;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive repository for Checkbook entity operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
@Repository
public interface CheckbookRepository extends R2dbcRepository<Checkbook, UUID> {

    /**
     * Finds all checkbooks ordered by creation date descending.
     *
     * @return Flux of checkbooks
     */
    @Query("SELECT * FROM treasury.checkbooks ORDER BY created_at DESC")
    Flux<Checkbook> findAllOrderByCreatedAtDesc();

    /**
     * Finds all checkbooks for a specific bank account.
     *
     * @param accountId the bank account ID
     * @return Flux of checkbooks
     */
    @Query("SELECT * FROM treasury.checkbooks WHERE bank_account_id = :accountId ORDER BY created_at DESC")
    Flux<Checkbook> findByBankAccountId(UUID accountId);

    /**
     * Finds the active checkbook for a bank account (oldest active one).
     *
     * @param accountId the bank account ID
     * @return the active checkbook if found
     */
    @Query("SELECT * FROM treasury.checkbooks WHERE bank_account_id = :accountId AND status = 'ACTIVE' ORDER BY created_at ASC LIMIT 1")
    Mono<Checkbook> findActiveByBankAccountId(UUID accountId);

    /**
     * Finds all checkbooks with a specific status.
     *
     * @param status the status to filter by
     * @return Flux of checkbooks
     */
    @Query("SELECT * FROM treasury.checkbooks WHERE status = :status ORDER BY created_at DESC")
    Flux<Checkbook> findByStatus(String status);

    /**
     * Atomically increments the current number.
     * Only increments if current_number is still within the valid range.
     *
     * @param id the checkbook ID
     * @return the number of rows affected (1 if successful, 0 if out of range)
     */
    @Modifying
    @Query("""
        UPDATE treasury.checkbooks
        SET current_number = current_number + 1,
            updated_at = CURRENT_TIMESTAMP,
            status = CASE WHEN current_number + 1 > end_number THEN 'FINISHED' ELSE status END
        WHERE id = :id AND current_number <= end_number AND status = 'ACTIVE'
        """)
    Mono<Long> incrementCurrentNumber(UUID id);

    /**
     * Checks if a checkbook exists with overlapping number range for the same account.
     *
     * @param accountId the bank account ID
     * @param startNumber the start of the range
     * @param endNumber the end of the range
     * @return true if overlapping checkbook exists
     */
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM treasury.checkbooks
            WHERE bank_account_id = :accountId
            AND status != 'CANCELLED'
            AND type = 'REEL'
            AND (
                (start_number <= :endNumber AND end_number >= :startNumber)
            )
        )
        """)
    Mono<Boolean> existsOverlappingRange(UUID accountId, Integer startNumber, Integer endNumber);

    /**
     * Finds the system checkbook (fictif).
     *
     * @return Mono of the system checkbook if exists
     */
    @Query("SELECT * FROM treasury.checkbooks WHERE is_system = true LIMIT 1")
    Mono<Checkbook> findByIsSystemTrue();

    /**
     * Finds checkbooks by type.
     *
     * @param type the checkbook type (REEL or FICTIF)
     * @return Flux of checkbooks
     */
    @Query("SELECT * FROM treasury.checkbooks WHERE type = :type ORDER BY created_at DESC")
    Flux<Checkbook> findByType(String type);

    /**
     * Finds all real (non-system) checkbooks.
     *
     * @return Flux of real checkbooks
     */
    @Query("SELECT * FROM treasury.checkbooks WHERE is_system = false ORDER BY created_at DESC")
    Flux<Checkbook> findAllRealCheckbooks();

    /**
     * Atomically increments the next_sequence for fictif checkbook.
     *
     * @param id the checkbook ID
     * @return the number of rows affected
     */
    @Modifying
    @Query("""
        UPDATE treasury.checkbooks
        SET next_sequence = next_sequence + 1,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = :id AND type = 'FICTIF' AND is_system = true
        """)
    Mono<Long> incrementNextSequence(UUID id);

    /**
     * Gets the current next_sequence value for the fictif checkbook.
     *
     * @return the next sequence number
     */
    @Query("SELECT next_sequence FROM treasury.checkbooks WHERE is_system = true LIMIT 1")
    Mono<Long> getNextSequence();
}
