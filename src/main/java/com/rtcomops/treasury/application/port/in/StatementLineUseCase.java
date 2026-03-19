package com.rtcomops.treasury.application.port.in;

import com.rtcomops.treasury.application.dto.request.CreateStatementLineRequest;
import com.rtcomops.treasury.application.dto.response.StatementLineResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Input port for StatementLine use cases.
 *
 * <p>Defines the contract for statement line business operations.
 * This port is implemented by the domain service and used by the infrastructure layer.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
public interface StatementLineUseCase {

    /**
     * Retrieves all lines for a bank statement.
     *
     * @param statementId the statement ID
     * @return Flux of StatementLineResponse DTOs
     */
    Flux<StatementLineResponse> findByStatementId(UUID statementId);

    /**
     * Retrieves a statement line by its ID.
     *
     * @param id the line ID
     * @return Mono of StatementLineResponse
     */
    Mono<StatementLineResponse> findById(UUID id);

    /**
     * Retrieves unmatched lines for a statement.
     *
     * @param statementId the statement ID
     * @return Flux of unmatched StatementLineResponse DTOs
     */
    Flux<StatementLineResponse> findUnmatchedByStatementId(UUID statementId);

    /**
     * Retrieves matched lines for a statement.
     *
     * @param statementId the statement ID
     * @return Flux of matched StatementLineResponse DTOs
     */
    Flux<StatementLineResponse> findMatchedByStatementId(UUID statementId);

    /**
     * Creates a new statement line.
     *
     * @param request the create request DTO
     * @return Mono of created StatementLineResponse
     */
    Mono<StatementLineResponse> create(CreateStatementLineRequest request);

    /**
     * Creates multiple statement lines in batch.
     *
     * @param statementId the statement ID
     * @param requests list of create request DTOs
     * @return Flux of created StatementLineResponse DTOs
     */
    Flux<StatementLineResponse> createBatch(UUID statementId, List<CreateStatementLineRequest> requests);

    /**
     * Deletes a statement line.
     *
     * @param id the line ID to delete
     * @return Mono that completes when deletion is done
     */
    Mono<Void> delete(UUID id);

    /**
     * Marks a line as ignored.
     *
     * @param id the line ID
     * @return Mono of updated StatementLineResponse
     */
    Mono<StatementLineResponse> ignore(UUID id);

    /**
     * Resets a line to unmatched status.
     *
     * @param id the line ID
     * @return Mono of updated StatementLineResponse
     */
    Mono<StatementLineResponse> resetToUnmatched(UUID id);

    /**
     * Marks a line as matched.
     *
     * @param id the line ID
     * @return Mono of updated StatementLineResponse
     */
    Mono<StatementLineResponse> markAsMatched(UUID id);
}
