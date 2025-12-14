package com.rtcomops.treasury.repository;

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
}
