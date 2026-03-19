package com.rtcomops.treasury.domain.port.out;

import com.rtcomops.treasury.domain.model.TransactionSequence;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Output port for TransactionSequence repository operations.
 *
 * <p>Defines the contract for persistence operations on TransactionSequence entities.
 * This port is used for generating unique transaction references.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
public interface TransactionSequenceRepositoryPort {

    /**
     * Finds a sequence by type code and year-month.
     *
     * @param typeCode the transaction type code
     * @param yearMonth the year-month in YYYYMM format
     * @return Mono containing the sequence if found, empty otherwise
     */
    Mono<TransactionSequence> findByTypeCodeAndYearMonth(String typeCode, String yearMonth);

    /**
     * Atomically increments the sequence number.
     * Creates a new record if none exists for the type/month combination.
     *
     * @param typeCode the transaction type code
     * @param yearMonth the year-month in YYYYMM format
     * @return Mono containing the number of affected rows
     */
    Mono<Long> incrementSequence(String typeCode, String yearMonth);

    /**
     * Finds the last sequence number for a type/month combination.
     *
     * @param typeCode the transaction type code
     * @param yearMonth the year-month in YYYYMM format
     * @return Mono containing the last sequence number
     */
    Mono<Integer> findLastSequence(String typeCode, String yearMonth);

    /**
     * Saves a transaction sequence (insert or update).
     *
     * @param sequence the sequence to save
     * @return Mono containing the saved sequence
     */
    Mono<TransactionSequence> save(TransactionSequence sequence);

    /**
     * Finds a sequence by its unique identifier.
     *
     * @param id the sequence ID
     * @return Mono containing the sequence if found, empty otherwise
     */
    Mono<TransactionSequence> findById(UUID id);
}
