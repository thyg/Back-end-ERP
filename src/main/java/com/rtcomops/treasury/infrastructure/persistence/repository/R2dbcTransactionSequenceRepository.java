package com.rtcomops.treasury.infrastructure.persistence.repository;

import com.rtcomops.treasury.infrastructure.persistence.entity.TransactionSequenceEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * R2DBC repository for TransactionSequenceEntity persistence operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
@Repository
public interface R2dbcTransactionSequenceRepository extends R2dbcRepository<TransactionSequenceEntity, UUID> {

    @Query("SELECT * FROM treasury.transaction_sequences WHERE type_code = :typeCode AND year_month = :yearMonth")
    Mono<TransactionSequenceEntity> findByTypeCodeAndYearMonth(String typeCode, String yearMonth);

    @Modifying
    @Query("INSERT INTO treasury.transaction_sequences (id, type_code, year_month, last_sequence) " +
           "VALUES (gen_random_uuid(), :typeCode, :yearMonth, 1) " +
           "ON CONFLICT (type_code, year_month) DO UPDATE SET last_sequence = treasury.transaction_sequences.last_sequence + 1")
    Mono<Long> incrementSequence(String typeCode, String yearMonth);

    @Query("SELECT last_sequence FROM treasury.transaction_sequences WHERE type_code = :typeCode AND year_month = :yearMonth")
    Mono<Integer> findLastSequence(String typeCode, String yearMonth);
}
