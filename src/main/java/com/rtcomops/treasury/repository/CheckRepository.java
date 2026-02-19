package com.rtcomops.treasury.repository;

import com.rtcomops.treasury.dto.response.CheckbookStatsResponse;
import com.rtcomops.treasury.entity.Check;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Reactive repository for Check entity operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Repository
public interface CheckRepository extends R2dbcRepository<Check, UUID> {

    @Query("SELECT * FROM treasury.checks ORDER BY issue_date DESC, created_at DESC")
    Flux<Check> findAllOrderByDateDesc();

    @Query("SELECT * FROM treasury.checks WHERE bank_account_id = :accountId ORDER BY issue_date DESC")
    Flux<Check> findByBankAccountId(UUID accountId);

    @Query("SELECT * FROM treasury.checks WHERE check_type = :checkType ORDER BY issue_date DESC")
    Flux<Check> findByCheckType(String checkType);

    @Query("SELECT * FROM treasury.checks WHERE status = :status ORDER BY issue_date DESC")
    Flux<Check> findByStatus(String status);

    @Query("SELECT * FROM treasury.checks WHERE check_type = :checkType AND status = :status ORDER BY issue_date DESC")
    Flux<Check> findByCheckTypeAndStatus(String checkType, String status);

    @Query("SELECT * FROM treasury.checks WHERE bank_account_id = :accountId AND check_type = :checkType ORDER BY issue_date DESC")
    Flux<Check> findByBankAccountIdAndCheckType(UUID accountId, String checkType);

    @Query("SELECT * FROM treasury.checks WHERE due_date <= :date AND status = 'PENDING' ORDER BY due_date ASC")
    Flux<Check> findPendingChecksDueBefore(LocalDate date);

    @Query("SELECT COUNT(*) > 0 FROM treasury.checks WHERE check_number = :checkNumber AND bank_account_id = :accountId AND check_type = :checkType")
    Mono<Boolean> existsByCheckNumberAndAccountAndType(String checkNumber, UUID accountId, String checkType);

    @Query("SELECT COUNT(*) > 0 FROM treasury.checks WHERE check_number = :checkNumber AND bank_account_id = :accountId AND check_type = :checkType AND id != :id")
    Mono<Boolean> existsByCheckNumberAndAccountAndTypeAndIdNot(String checkNumber, UUID accountId, String checkType, UUID id);

    /**
     * Finds all checks belonging to a specific checkbook.
     *
     * @param checkbookId the ID of the checkbook
     * @return Flux of checks from that checkbook, ordered by issue date
     */
    @Query("SELECT * FROM treasury.checks WHERE checkbook_id = :checkbookId ORDER BY issue_date DESC, created_at DESC")
    Flux<Check> findByCheckbookId(UUID checkbookId);

    /**
     * Gathers statistics for a specific checkbook.
     *
     * @param checkbookId the ID of the checkbook
     * @return Mono containing the calculated statistics
     */
    @Query("SELECT " +
           "  COUNT(*) AS used_checks_count, " +
           "  COALESCE(SUM(CASE WHEN status IN ('ISSUED', 'RECEIVED', 'DEPOSITED', 'IN_PROGRESS') THEN amount ELSE 0 END), 0) AS total_amount_issued, " +
           "  COALESCE(SUM(CASE WHEN status = 'CASHED' THEN amount ELSE 0 END), 0) AS total_amount_cashed " +
           "FROM treasury.checks " +
           "WHERE checkbook_id = :checkbookId")
    Mono<CheckbookStatsResponse> getStatsByCheckbookId(UUID checkbookId);

    // =========================================================================
    // STATISTIQUES GLOBALES DES CHÈQUES
    // =========================================================================

    /**
     * Counts checks by status (excluding CANCELLED).
     */
    @Query("SELECT COUNT(*) FROM treasury.checks WHERE status = :status")
    Mono<Integer> countByStatus(String status);

    /**
     * Sums amounts by status.
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM treasury.checks WHERE status = :status")
    Mono<java.math.BigDecimal> sumAmountByStatus(String status);

    /**
     * Counts checks by type (ISSUED or RECEIVED), excluding CANCELLED.
     */
    @Query("SELECT COUNT(*) FROM treasury.checks WHERE check_type = :checkType AND status != 'CANCELLED'")
    Mono<Integer> countByCheckType(String checkType);

    /**
     * Sums amounts by type, excluding CANCELLED.
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM treasury.checks WHERE check_type = :checkType AND status != 'CANCELLED'")
    Mono<java.math.BigDecimal> sumAmountByCheckType(String checkType);

    /**
     * Counts total checks excluding CANCELLED.
     */
    @Query("SELECT COUNT(*) FROM treasury.checks WHERE status != 'CANCELLED'")
    Mono<Integer> countAllExcludingCancelled();

    /**
     * Sums total amount excluding CANCELLED.
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM treasury.checks WHERE status != 'CANCELLED'")
    Mono<java.math.BigDecimal> sumTotalAmountExcludingCancelled();

    /**
     * Counts overdue checks: checks with due_date in the past and not yet cashed/rejected/cancelled.
     * Only applies to RECEIVED checks that are PENDING, RECEIVED, DEPOSITED, or IN_PROGRESS.
     */
    @Query("SELECT COUNT(*) FROM treasury.checks " +
           "WHERE check_type = 'RECEIVED' " +
           "AND due_date IS NOT NULL " +
           "AND due_date < CURRENT_DATE " +
           "AND status IN ('PENDING', 'RECEIVED', 'DEPOSITED', 'IN_PROGRESS')")
    Mono<Integer> countOverdueChecks();

    /**
     * Sums amount of overdue checks.
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM treasury.checks " +
           "WHERE check_type = 'RECEIVED' " +
           "AND due_date IS NOT NULL " +
           "AND due_date < CURRENT_DATE " +
           "AND status IN ('PENDING', 'RECEIVED', 'DEPOSITED', 'IN_PROGRESS')")
    Mono<java.math.BigDecimal> sumOverdueAmount();

    /**
     * Finds all overdue checks (for listing).
     */
    @Query("SELECT * FROM treasury.checks " +
           "WHERE check_type = 'RECEIVED' " +
           "AND due_date IS NOT NULL " +
           "AND due_date < CURRENT_DATE " +
           "AND status IN ('PENDING', 'RECEIVED', 'DEPOSITED', 'IN_PROGRESS') " +
           "ORDER BY due_date ASC")
    Flux<Check> findOverdueChecks();

    // =========================================================================
    // CHECK DEPOSITS (REMISES EN LOT)
    // =========================================================================

    /**
     * Finds all checks belonging to a specific check deposit batch.
     *
     * @param checkDepositId the ID of the check deposit
     * @return Flux of checks in that deposit batch
     */
    @Query("SELECT * FROM treasury.checks WHERE check_deposit_id = :checkDepositId ORDER BY check_number ASC")
    Flux<Check> findByCheckDepositId(java.util.UUID checkDepositId);

}
