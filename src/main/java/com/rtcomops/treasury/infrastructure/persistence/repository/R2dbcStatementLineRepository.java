package com.rtcomops.treasury.infrastructure.persistence.repository;

import com.rtcomops.treasury.infrastructure.persistence.entity.StatementLineEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * R2DBC repository for StatementLineEntity persistence operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Repository
public interface R2dbcStatementLineRepository extends R2dbcRepository<StatementLineEntity, UUID> {

    @Query("SELECT * FROM treasury.statement_lines WHERE bank_statement_id = :statementId ORDER BY line_number ASC")
    Flux<StatementLineEntity> findByBankStatementIdOrderByLineNumber(UUID statementId);

    @Query("SELECT * FROM treasury.statement_lines WHERE bank_statement_id = :statementId AND reconciliation_status = :status ORDER BY line_number ASC")
    Flux<StatementLineEntity> findByStatementIdAndStatus(UUID statementId, String status);

    @Query("SELECT * FROM treasury.statement_lines WHERE bank_statement_id = :statementId AND reconciliation_status = 'UNMATCHED' ORDER BY line_number ASC")
    Flux<StatementLineEntity> findUnmatchedByStatementId(UUID statementId);

    @Query("SELECT * FROM treasury.statement_lines WHERE bank_statement_id = :statementId AND reconciliation_status = 'MATCHED' ORDER BY line_number ASC")
    Flux<StatementLineEntity> findMatchedByStatementId(UUID statementId);

    @Query("SELECT MAX(line_number) FROM treasury.statement_lines WHERE bank_statement_id = :statementId")
    Mono<Integer> findMaxLineNumberByStatementId(UUID statementId);

    @Query("SELECT COUNT(*) FROM treasury.statement_lines WHERE bank_statement_id = :statementId")
    Mono<Long> countByStatementId(UUID statementId);

    @Query("SELECT COUNT(*) FROM treasury.statement_lines WHERE bank_statement_id = :statementId AND reconciliation_status = 'MATCHED'")
    Mono<Long> countMatchedByStatementId(UUID statementId);

    @Query("SELECT * FROM treasury.statement_lines sl " +
           "JOIN treasury.bank_statements bs ON sl.bank_statement_id = bs.id " +
           "WHERE bs.bank_account_id = :accountId " +
           "AND sl.amount = :amount " +
           "AND sl.direction = :direction " +
           "AND sl.reconciliation_status = 'UNMATCHED' " +
           "ORDER BY sl.transaction_date DESC")
    Flux<StatementLineEntity> findUnmatchedByAccountAndAmountAndDirection(UUID accountId, BigDecimal amount, String direction);

    @Query("SELECT * FROM treasury.statement_lines sl " +
           "JOIN treasury.bank_statements bs ON sl.bank_statement_id = bs.id " +
           "WHERE bs.bank_account_id = :accountId " +
           "AND sl.transaction_date BETWEEN :startDate AND :endDate " +
           "AND sl.reconciliation_status = 'UNMATCHED' " +
           "ORDER BY sl.transaction_date ASC")
    Flux<StatementLineEntity> findUnmatchedByAccountAndDateRange(UUID accountId, LocalDate startDate, LocalDate endDate);
}
