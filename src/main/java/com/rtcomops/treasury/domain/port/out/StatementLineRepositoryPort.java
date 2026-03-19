package com.rtcomops.treasury.domain.port.out;

import com.rtcomops.treasury.domain.model.StatementLine;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Output port for StatementLine repository operations.
 *
 * <p>Defines the contract for persistence operations on StatementLine entities.
 * This port is implemented by the infrastructure layer adapter.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
public interface StatementLineRepositoryPort {

    /**
     * Finds a statement line by its unique identifier.
     *
     * @param id the line ID
     * @return Mono containing the line if found, empty otherwise
     */
    Mono<StatementLine> findById(UUID id);

    /**
     * Finds all lines for a bank statement ordered by line number.
     *
     * @param statementId the bank statement ID
     * @return Flux of lines for the statement
     */
    Flux<StatementLine> findByBankStatementIdOrderByLineNumber(UUID statementId);

    /**
     * Finds lines by statement ID and reconciliation status.
     *
     * @param statementId the bank statement ID
     * @param status the reconciliation status
     * @return Flux of matching lines
     */
    Flux<StatementLine> findByStatementIdAndStatus(UUID statementId, String status);

    /**
     * Finds unmatched lines for a statement.
     *
     * @param statementId the bank statement ID
     * @return Flux of unmatched lines
     */
    Flux<StatementLine> findUnmatchedByStatementId(UUID statementId);

    /**
     * Finds matched lines for a statement.
     *
     * @param statementId the bank statement ID
     * @return Flux of matched lines
     */
    Flux<StatementLine> findMatchedByStatementId(UUID statementId);

    /**
     * Finds the maximum line number for a statement.
     *
     * @param statementId the bank statement ID
     * @return Mono containing the max line number
     */
    Mono<Integer> findMaxLineNumberByStatementId(UUID statementId);

    /**
     * Counts lines for a statement.
     *
     * @param statementId the bank statement ID
     * @return Mono containing the count
     */
    Mono<Long> countByStatementId(UUID statementId);

    /**
     * Counts matched lines for a statement.
     *
     * @param statementId the bank statement ID
     * @return Mono containing the count
     */
    Mono<Long> countMatchedByStatementId(UUID statementId);

    /**
     * Finds unmatched lines by account, amount and direction.
     *
     * @param accountId the bank account ID
     * @param amount the transaction amount
     * @param direction the transaction direction
     * @return Flux of matching lines
     */
    Flux<StatementLine> findUnmatchedByAccountAndAmountAndDirection(UUID accountId, BigDecimal amount, String direction);

    /**
     * Finds unmatched lines by account and date range.
     *
     * @param accountId the bank account ID
     * @param startDate the start date
     * @param endDate the end date
     * @return Flux of matching lines
     */
    Flux<StatementLine> findUnmatchedByAccountAndDateRange(UUID accountId, LocalDate startDate, LocalDate endDate);

    /**
     * Saves a statement line (insert or update).
     *
     * @param line the line to save
     * @return Mono containing the saved line
     */
    Mono<StatementLine> save(StatementLine line);

    /**
     * Saves multiple statement lines.
     *
     * @param lines the lines to save
     * @return Flux of saved lines
     */
    Flux<StatementLine> saveAll(Iterable<StatementLine> lines);

    /**
     * Deletes a statement line.
     *
     * @param line the line to delete
     * @return Mono that completes when deletion is done
     */
    Mono<Void> delete(StatementLine line);

    /**
     * Deletes all lines for a statement.
     *
     * @param statementId the bank statement ID
     * @return Mono that completes when deletion is done
     */
    Mono<Void> deleteByStatementId(UUID statementId);
}
