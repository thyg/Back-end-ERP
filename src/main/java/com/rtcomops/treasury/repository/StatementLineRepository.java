package com.rtcomops.treasury.repository;

import com.rtcomops.treasury.entity.StatementLine;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Reactive repository for StatementLine entity operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Repository
public interface StatementLineRepository extends R2dbcRepository<StatementLine, UUID> {

    @Query("SELECT * FROM treasury.statement_lines WHERE bank_statement_id = :statementId ORDER BY line_number ASC")
    Flux<StatementLine> findByBankStatementIdOrderByLineNumber(UUID statementId);

    @Query("SELECT * FROM treasury.statement_lines WHERE bank_statement_id = :statementId AND reconciliation_status = :status ORDER BY line_number ASC")
    Flux<StatementLine> findByStatementIdAndStatus(UUID statementId, String status);

    @Query("SELECT * FROM treasury.statement_lines WHERE bank_statement_id = :statementId AND reconciliation_status = 'UNMATCHED' ORDER BY line_number ASC")
    Flux<StatementLine> findUnmatchedByStatementId(UUID statementId);

    @Query("SELECT * FROM treasury.statement_lines WHERE bank_statement_id = :statementId AND reconciliation_status = 'MATCHED' ORDER BY line_number ASC")
    Flux<StatementLine> findMatchedByStatementId(UUID statementId);

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
    Flux<StatementLine> findUnmatchedByAccountAndAmountAndDirection(UUID accountId, BigDecimal amount, String direction);

    @Query("SELECT * FROM treasury.statement_lines sl " +
           "JOIN treasury.bank_statements bs ON sl.bank_statement_id = bs.id " +
           "WHERE bs.bank_account_id = :accountId " +
           "AND sl.transaction_date BETWEEN :startDate AND :endDate " +
           "AND sl.reconciliation_status = 'UNMATCHED' " +
           "ORDER BY sl.transaction_date ASC")
    Flux<StatementLine> findUnmatchedByAccountAndDateRange(UUID accountId, LocalDate startDate, LocalDate endDate);
}
