package com.rtcomops.treasury.domain.port.out;

import com.rtcomops.treasury.domain.model.Checkbook;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Output port for Checkbook repository operations.
 *
 * <p>Defines the contract for persistence operations on Checkbook entities.
 * This port is implemented by the infrastructure layer adapter.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
public interface CheckbookRepositoryPort {

    /**
     * Finds a checkbook by its unique identifier.
     *
     * @param id the checkbook ID
     * @return Mono containing the checkbook if found, empty otherwise
     */
    Mono<Checkbook> findById(UUID id);

    /**
     * Finds all checkbooks ordered by creation date descending.
     *
     * @return Flux of all checkbooks
     */
    Flux<Checkbook> findAllOrderByCreatedAtDesc();

    /**
     * Finds all checkbooks for a specific bank account.
     *
     * @param accountId the bank account ID
     * @return Flux of checkbooks for the account
     */
    Flux<Checkbook> findByBankAccountId(UUID accountId);

    /**
     * Finds the active checkbook for a bank account.
     *
     * @param accountId the bank account ID
     * @return Mono containing the active checkbook if found
     */
    Mono<Checkbook> findActiveByBankAccountId(UUID accountId);

    /**
     * Finds all checkbooks with a specific status.
     *
     * @param status the status to filter by
     * @return Flux of checkbooks with the specified status
     */
    Flux<Checkbook> findByStatus(String status);

    /**
     * Finds the system checkbook (isSystem=true).
     *
     * @return Mono containing the system checkbook if found
     */
    Mono<Checkbook> findByIsSystemTrue();

    /**
     * Finds checkbooks by type.
     *
     * @param type the checkbook type (REEL or FICTIF)
     * @return Flux of checkbooks with the specified type
     */
    Flux<Checkbook> findByType(String type);

    /**
     * Finds all real (non-system) checkbooks.
     *
     * @return Flux of real checkbooks
     */
    Flux<Checkbook> findAllRealCheckbooks();

    /**
     * Checks if a checkbook exists with overlapping number range for the same account.
     *
     * @param accountId the bank account ID
     * @param startNumber the start of the range
     * @param endNumber the end of the range
     * @return Mono<Boolean> true if overlapping checkbook exists
     */
    Mono<Boolean> existsOverlappingRange(UUID accountId, Integer startNumber, Integer endNumber);

    /**
     * Atomically increments the current number of a checkbook.
     * Only increments if current_number is still within the valid range.
     *
     * @param id the checkbook ID
     * @return Mono containing the number of rows affected (1 if successful, 0 if out of range)
     */
    Mono<Long> incrementCurrentNumber(UUID id);

    /**
     * Atomically increments the next_sequence for fictif checkbook.
     *
     * @param id the checkbook ID
     * @return Mono containing the number of rows affected
     */
    Mono<Long> incrementNextSequence(UUID id);

    /**
     * Gets the current next_sequence value for the fictif checkbook.
     *
     * @return Mono containing the next sequence number
     */
    Mono<Long> getNextSequence();

    /**
     * Saves a checkbook (insert or update).
     *
     * @param checkbook the checkbook to save
     * @return Mono containing the saved checkbook
     */
    Mono<Checkbook> save(Checkbook checkbook);

    /**
     * Deletes a checkbook.
     *
     * @param checkbook the checkbook to delete
     * @return Mono that completes when deletion is done
     */
    Mono<Void> delete(Checkbook checkbook);
}
