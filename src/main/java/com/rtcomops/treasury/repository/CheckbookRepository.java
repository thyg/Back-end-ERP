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
            AND (
                (start_number <= :endNumber AND end_number >= :startNumber)
            )
        )
        """)
    Mono<Boolean> existsOverlappingRange(UUID accountId, Integer startNumber, Integer endNumber);
}
