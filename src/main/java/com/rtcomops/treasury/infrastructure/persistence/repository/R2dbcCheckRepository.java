package com.rtcomops.treasury.infrastructure.persistence.repository;

import com.rtcomops.treasury.infrastructure.persistence.entity.CheckEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * R2DBC repository for CheckEntity persistence operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Repository
public interface R2dbcCheckRepository extends R2dbcRepository<CheckEntity, UUID> {

    @Query("SELECT * FROM treasury.checks ORDER BY issue_date DESC, created_at DESC")
    Flux<CheckEntity> findAllOrderByDateDesc();

    @Query("SELECT * FROM treasury.checks WHERE bank_account_id = :accountId ORDER BY issue_date DESC")
    Flux<CheckEntity> findByBankAccountId(UUID accountId);

    @Query("SELECT * FROM treasury.checks WHERE check_type = :checkType ORDER BY issue_date DESC")
    Flux<CheckEntity> findByCheckType(String checkType);

    @Query("SELECT * FROM treasury.checks WHERE status = :status ORDER BY issue_date DESC")
    Flux<CheckEntity> findByStatus(String status);

    @Query("SELECT * FROM treasury.checks WHERE check_type = :checkType AND status = :status ORDER BY issue_date DESC")
    Flux<CheckEntity> findByCheckTypeAndStatus(String checkType, String status);

    @Query("SELECT * FROM treasury.checks WHERE bank_account_id = :accountId AND check_type = :checkType ORDER BY issue_date DESC")
    Flux<CheckEntity> findByBankAccountIdAndCheckType(UUID accountId, String checkType);

    @Query("SELECT * FROM treasury.checks WHERE due_date <= :date AND status = 'PENDING' ORDER BY due_date ASC")
    Flux<CheckEntity> findPendingChecksDueBefore(LocalDate date);

    @Query("SELECT COUNT(*) > 0 FROM treasury.checks WHERE check_number = :checkNumber AND bank_account_id = :accountId AND check_type = :checkType")
    Mono<Boolean> existsByCheckNumberAndAccountAndType(String checkNumber, UUID accountId, String checkType);

    @Query("SELECT COUNT(*) > 0 FROM treasury.checks WHERE check_number = :checkNumber AND bank_account_id = :accountId AND check_type = :checkType AND id != :id")
    Mono<Boolean> existsByCheckNumberAndAccountAndTypeAndIdNot(String checkNumber, UUID accountId, String checkType, UUID id);

    @Query("SELECT * FROM treasury.checks WHERE checkbook_id = :checkbookId ORDER BY issue_date DESC, created_at DESC")
    Flux<CheckEntity> findByCheckbookId(UUID checkbookId);

    @Query("SELECT * FROM treasury.checks WHERE check_deposit_id = :checkDepositId ORDER BY check_number ASC")
    Flux<CheckEntity> findByCheckDepositId(UUID checkDepositId);

    @Query("SELECT * FROM treasury.checks WHERE check_type = 'RECEIVED' AND due_date IS NOT NULL AND due_date < CURRENT_DATE AND status IN ('PENDING', 'RECEIVED', 'DEPOSITED', 'IN_PROGRESS') ORDER BY due_date ASC")
    Flux<CheckEntity> findOverdueChecks();

    // Statistics methods
    @Query("SELECT COUNT(*) FROM treasury.checks WHERE status = :status")
    Mono<Integer> countByStatus(String status);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM treasury.checks WHERE status = :status")
    Mono<BigDecimal> sumAmountByStatus(String status);

    @Query("SELECT COUNT(*) FROM treasury.checks WHERE check_type = :checkType AND status != 'CANCELLED'")
    Mono<Integer> countByCheckType(String checkType);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM treasury.checks WHERE check_type = :checkType AND status != 'CANCELLED'")
    Mono<BigDecimal> sumAmountByCheckType(String checkType);

    @Query("SELECT COUNT(*) FROM treasury.checks WHERE status != 'CANCELLED'")
    Mono<Integer> countAllExcludingCancelled();

    @Query("SELECT COALESCE(SUM(amount), 0) FROM treasury.checks WHERE status != 'CANCELLED'")
    Mono<BigDecimal> sumTotalAmountExcludingCancelled();

    @Query("SELECT COUNT(*) FROM treasury.checks WHERE check_type = 'RECEIVED' AND due_date IS NOT NULL AND due_date < CURRENT_DATE AND status IN ('PENDING', 'RECEIVED', 'DEPOSITED', 'IN_PROGRESS')")
    Mono<Integer> countOverdueChecks();

    @Query("SELECT COALESCE(SUM(amount), 0) FROM treasury.checks WHERE check_type = 'RECEIVED' AND due_date IS NOT NULL AND due_date < CURRENT_DATE AND status IN ('PENDING', 'RECEIVED', 'DEPOSITED', 'IN_PROGRESS')")
    Mono<BigDecimal> sumOverdueAmount();
}
