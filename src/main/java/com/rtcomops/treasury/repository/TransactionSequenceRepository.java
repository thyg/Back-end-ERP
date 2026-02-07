package com.rtcomops.treasury.repository;

import com.rtcomops.treasury.entity.TransactionSequence;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive repository for TransactionSequence entity operations.
 *
 * <p>Provides methods to manage sequence numbers for automatic reference generation.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
@Repository
public interface TransactionSequenceRepository extends R2dbcRepository<TransactionSequence, UUID> {

    /**
     * Finds a sequence by type code and year-month.
     *
     * @param typeCode the transaction type code
     * @param yearMonth the year-month in YYYYMM format
     * @return the sequence if found
     */
    @Query("SELECT * FROM treasury.transaction_sequences WHERE type_code = :typeCode AND year_month = :yearMonth")
    Mono<TransactionSequence> findByTypeCodeAndYearMonth(String typeCode, String yearMonth);

    /**
     * Atomically increments and returns the next sequence number.
     * Creates a new record if none exists for the type/month combination.
     *
     * @param typeCode the transaction type code
     * @param yearMonth the year-month in YYYYMM format
     * @return the next sequence number
     */
    @Modifying
@Query("INSERT INTO treasury.transaction_sequences (id, type_code, year_month, last_sequence) " +
       "VALUES (gen_random_uuid(), :typeCode, :yearMonth, 1) " +
       "ON CONFLICT (type_code, year_month) DO UPDATE SET last_sequence = treasury.transaction_sequences.last_sequence + 1")
Mono<Long> incrementSequence(String typeCode, String yearMonth);

@Query("SELECT last_sequence FROM treasury.transaction_sequences WHERE type_code = :typeCode AND year_month = :yearMonth")
Mono<Integer> findLastSequence(String typeCode, String yearMonth);
}
