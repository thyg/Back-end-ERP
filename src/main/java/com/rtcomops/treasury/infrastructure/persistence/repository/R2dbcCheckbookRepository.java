package com.rtcomops.treasury.infrastructure.persistence.repository;

import com.rtcomops.treasury.infrastructure.persistence.entity.CheckbookEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * R2DBC repository for CheckbookEntity persistence operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
@Repository
public interface R2dbcCheckbookRepository extends R2dbcRepository<CheckbookEntity, UUID> {

    @Query("SELECT * FROM treasury.checkbooks ORDER BY created_at DESC")
    Flux<CheckbookEntity> findAllOrderByCreatedAtDesc();

    @Query("SELECT * FROM treasury.checkbooks WHERE bank_account_id = :accountId ORDER BY created_at DESC")
    Flux<CheckbookEntity> findByBankAccountId(UUID accountId);

    @Query("SELECT * FROM treasury.checkbooks WHERE bank_account_id = :accountId AND status = 'ACTIVE' ORDER BY created_at ASC LIMIT 1")
    Mono<CheckbookEntity> findActiveByBankAccountId(UUID accountId);

    @Query("SELECT * FROM treasury.checkbooks WHERE status = :status ORDER BY created_at DESC")
    Flux<CheckbookEntity> findByStatus(String status);

    @Modifying
    @Query("""
        UPDATE treasury.checkbooks
        SET current_number = current_number + 1,
            updated_at = CURRENT_TIMESTAMP,
            status = CASE WHEN current_number + 1 > end_number THEN 'FINISHED' ELSE status END
        WHERE id = :id AND current_number <= end_number AND status = 'ACTIVE'
        """)
    Mono<Long> incrementCurrentNumber(UUID id);

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

    @Query("SELECT * FROM treasury.checkbooks WHERE is_system = true LIMIT 1")
    Mono<CheckbookEntity> findByIsSystemTrue();

    @Query("SELECT * FROM treasury.checkbooks WHERE type = :type ORDER BY created_at DESC")
    Flux<CheckbookEntity> findByType(String type);

    @Query("SELECT * FROM treasury.checkbooks WHERE is_system = false ORDER BY created_at DESC")
    Flux<CheckbookEntity> findAllRealCheckbooks();

    @Modifying
    @Query("""
        UPDATE treasury.checkbooks
        SET next_sequence = next_sequence + 1,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = :id AND type = 'FICTIF' AND is_system = true
        """)
    Mono<Long> incrementNextSequence(UUID id);

    @Query("SELECT next_sequence FROM treasury.checkbooks WHERE is_system = true LIMIT 1")
    Mono<Long> getNextSequence();
}
