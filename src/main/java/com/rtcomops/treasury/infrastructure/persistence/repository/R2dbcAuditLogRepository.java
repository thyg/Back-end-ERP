package com.rtcomops.treasury.infrastructure.persistence.repository;

import com.rtcomops.treasury.infrastructure.persistence.entity.AuditLogEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * R2DBC repository for AuditLogEntity persistence operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-13
 */
@Repository
public interface R2dbcAuditLogRepository extends R2dbcRepository<AuditLogEntity, UUID> {

    // Basic queries
    @Query("SELECT * FROM treasury.audit_logs ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<AuditLogEntity> findAllPaginated(int limit, long offset);

    @Query("SELECT COUNT(*) FROM treasury.audit_logs")
    Mono<Long> countAll();

    // Filter by module
    @Query("SELECT * FROM treasury.audit_logs WHERE module = :module ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<AuditLogEntity> findByModule(String module, int limit, long offset);

    @Query("SELECT COUNT(*) FROM treasury.audit_logs WHERE module = :module")
    Mono<Long> countByModule(String module);

    // Filter by action
    @Query("SELECT * FROM treasury.audit_logs WHERE action = :action ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<AuditLogEntity> findByAction(String action, int limit, long offset);

    @Query("SELECT COUNT(*) FROM treasury.audit_logs WHERE action = :action")
    Mono<Long> countByAction(String action);

    // Filter by entity
    @Query("SELECT * FROM treasury.audit_logs WHERE entity_id = :entityId ORDER BY created_at DESC")
    Flux<AuditLogEntity> findByEntityId(UUID entityId);

    @Query("SELECT * FROM treasury.audit_logs WHERE entity_reference ILIKE '%' || :reference || '%' ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<AuditLogEntity> findByEntityReferenceContaining(String reference, int limit, long offset);

    // Filter by user
    @Query("SELECT * FROM treasury.audit_logs WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<AuditLogEntity> findByUserId(UUID userId, int limit, long offset);

    @Query("SELECT * FROM treasury.audit_logs WHERE user_name ILIKE '%' || :userName || '%' ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<AuditLogEntity> findByUserNameContaining(String userName, int limit, long offset);

    // Filter by date
    @Query("SELECT * FROM treasury.audit_logs WHERE created_at BETWEEN :startDate AND :endDate ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<AuditLogEntity> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, int limit, long offset);

    @Query("SELECT COUNT(*) FROM treasury.audit_logs WHERE created_at BETWEEN :startDate AND :endDate")
    Mono<Long> countByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT * FROM treasury.audit_logs WHERE DATE(created_at) = CURRENT_DATE ORDER BY created_at DESC")
    Flux<AuditLogEntity> findToday();

    // Combined filters
    @Query("SELECT * FROM treasury.audit_logs WHERE module = :module AND action = :action ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<AuditLogEntity> findByModuleAndAction(String module, String action, int limit, long offset);

    @Query("SELECT * FROM treasury.audit_logs WHERE module = :module AND created_at BETWEEN :startDate AND :endDate ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<AuditLogEntity> findByModuleAndDateRange(String module, LocalDateTime startDate, LocalDateTime endDate, int limit, long offset);

    // Global search
    @Query("SELECT * FROM treasury.audit_logs WHERE description ILIKE '%' || :search || '%' OR entity_reference ILIKE '%' || :search || '%' ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<AuditLogEntity> searchByDescriptionOrReference(String search, int limit, long offset);

    @Query("SELECT COUNT(*) FROM treasury.audit_logs WHERE description ILIKE '%' || :search || '%' OR entity_reference ILIKE '%' || :search || '%'")
    Mono<Long> countSearchResults(String search);

    // Cleanup
    @Query("DELETE FROM treasury.audit_logs WHERE created_at < :beforeDate")
    Mono<Long> deleteOlderThan(LocalDateTime beforeDate);
}
