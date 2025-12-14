package com.rtcomops.treasury.repository;

import com.rtcomops.treasury.entity.AuditLog;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Reactive repository for AuditLog entity operations.
 *
 * <p>Provides methods for querying audit logs with various filters
 * including module, action, entity, user, and date range.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-13
 */
@Repository
public interface AuditLogRepository extends R2dbcRepository<AuditLog, UUID> {

    // =========================================================================
    // REQUÊTES DE BASE
    // =========================================================================

    /**
     * Find all audit logs ordered by creation date (newest first).
     */
    @Query("SELECT * FROM treasury.audit_logs ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<AuditLog> findAllPaginated(int limit, long offset);

    /**
     * Count total audit logs.
     */
    @Query("SELECT COUNT(*) FROM treasury.audit_logs")
    Mono<Long> countAll();

    // =========================================================================
    // FILTRES PAR MODULE
    // =========================================================================

    /**
     * Find audit logs by module.
     */
    @Query("SELECT * FROM treasury.audit_logs WHERE module = :module ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<AuditLog> findByModule(String module, int limit, long offset);

    /**
     * Count audit logs by module.
     */
    @Query("SELECT COUNT(*) FROM treasury.audit_logs WHERE module = :module")
    Mono<Long> countByModule(String module);

    // =========================================================================
    // FILTRES PAR ACTION
    // =========================================================================

    /**
     * Find audit logs by action.
     */
    @Query("SELECT * FROM treasury.audit_logs WHERE action = :action ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<AuditLog> findByAction(String action, int limit, long offset);

    /**
     * Count audit logs by action.
     */
    @Query("SELECT COUNT(*) FROM treasury.audit_logs WHERE action = :action")
    Mono<Long> countByAction(String action);

    // =========================================================================
    // FILTRES PAR ENTITÉ
    // =========================================================================

    /**
     * Find audit logs for a specific entity.
     */
    @Query("SELECT * FROM treasury.audit_logs WHERE entity_id = :entityId ORDER BY created_at DESC")
    Flux<AuditLog> findByEntityId(UUID entityId);

    /**
     * Find audit logs by entity reference (partial match).
     */
    @Query("SELECT * FROM treasury.audit_logs WHERE entity_reference ILIKE '%' || :reference || '%' ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<AuditLog> findByEntityReferenceContaining(String reference, int limit, long offset);

    // =========================================================================
    // FILTRES PAR UTILISATEUR
    // =========================================================================

    /**
     * Find audit logs by user ID.
     */
    @Query("SELECT * FROM treasury.audit_logs WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<AuditLog> findByUserId(UUID userId, int limit, long offset);

    /**
     * Find audit logs by user name (partial match).
     */
    @Query("SELECT * FROM treasury.audit_logs WHERE user_name ILIKE '%' || :userName || '%' ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<AuditLog> findByUserNameContaining(String userName, int limit, long offset);

    // =========================================================================
    // FILTRES PAR DATE
    // =========================================================================

    /**
     * Find audit logs within a date range.
     */
    @Query("SELECT * FROM treasury.audit_logs WHERE created_at BETWEEN :startDate AND :endDate ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<AuditLog> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, int limit, long offset);

    /**
     * Count audit logs within a date range.
     */
    @Query("SELECT COUNT(*) FROM treasury.audit_logs WHERE created_at BETWEEN :startDate AND :endDate")
    Mono<Long> countByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find audit logs from today.
     */
    @Query("SELECT * FROM treasury.audit_logs WHERE DATE(created_at) = CURRENT_DATE ORDER BY created_at DESC")
    Flux<AuditLog> findToday();

    // =========================================================================
    // FILTRES COMBINÉS
    // =========================================================================

    /**
     * Find audit logs by module and action.
     */
    @Query("SELECT * FROM treasury.audit_logs WHERE module = :module AND action = :action ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<AuditLog> findByModuleAndAction(String module, String action, int limit, long offset);

    /**
     * Find audit logs by module within date range.
     */
    @Query("SELECT * FROM treasury.audit_logs WHERE module = :module AND created_at BETWEEN :startDate AND :endDate ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<AuditLog> findByModuleAndDateRange(String module, LocalDateTime startDate, LocalDateTime endDate, int limit, long offset);

    // =========================================================================
    // RECHERCHE GLOBALE
    // =========================================================================

    /**
     * Search audit logs by description or entity reference.
     */
    @Query("SELECT * FROM treasury.audit_logs WHERE description ILIKE '%' || :search || '%' OR entity_reference ILIKE '%' || :search || '%' ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<AuditLog> searchByDescriptionOrReference(String search, int limit, long offset);

    /**
     * Count search results.
     */
    @Query("SELECT COUNT(*) FROM treasury.audit_logs WHERE description ILIKE '%' || :search || '%' OR entity_reference ILIKE '%' || :search || '%'")
    Mono<Long> countSearchResults(String search);

    // =========================================================================
    // STATISTIQUES
    // =========================================================================

    /**
     * Count audit logs by module (for stats).
     */
    @Query("SELECT module, COUNT(*) as count FROM treasury.audit_logs GROUP BY module ORDER BY count DESC")
    Flux<Object[]> countGroupByModule();

    /**
     * Count audit logs by action (for stats).
     */
    @Query("SELECT action, COUNT(*) as count FROM treasury.audit_logs GROUP BY action ORDER BY count DESC")
    Flux<Object[]> countGroupByAction();

    /**
     * Count audit logs per day for the last N days.
     */
    @Query("SELECT DATE(created_at) as date, COUNT(*) as count FROM treasury.audit_logs WHERE created_at >= :since GROUP BY DATE(created_at) ORDER BY date DESC")
    Flux<Object[]> countPerDaySince(LocalDateTime since);

    // =========================================================================
    // NETTOYAGE
    // =========================================================================

    /**
     * Delete audit logs older than a specific date.
     * Used for archiving/cleanup.
     */
    @Query("DELETE FROM treasury.audit_logs WHERE created_at < :beforeDate")
    Mono<Long> deleteOlderThan(LocalDateTime beforeDate);
}