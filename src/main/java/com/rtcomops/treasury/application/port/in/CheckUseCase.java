package com.rtcomops.treasury.application.port.in;

import com.rtcomops.treasury.application.dto.request.CreateCheckRequest;
import com.rtcomops.treasury.application.dto.request.UpdateCheckRequest;
import com.rtcomops.treasury.application.dto.response.CheckResponse;
import com.rtcomops.treasury.application.dto.response.CheckStatsResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Input port for Check use cases.
 *
 * <p>Defines the contract for check business operations.
 * This port is implemented by the domain service and used by the infrastructure layer.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
public interface CheckUseCase {

    /**
     * Retrieves all checks with optional filter by checkbook ID.
     *
     * @param checkbookId optional checkbook ID filter
     * @return Flux of CheckResponse DTOs
     */
    Flux<CheckResponse> findAll(UUID checkbookId);

    /**
     * Retrieves a check by its ID.
     *
     * @param id the check ID
     * @return Mono of CheckResponse
     */
    Mono<CheckResponse> findById(UUID id);

    /**
     * Retrieves checks by type (ISSUED or RECEIVED).
     *
     * @param checkType the check type
     * @return Flux of CheckResponse DTOs
     */
    Flux<CheckResponse> findByType(String checkType);

    /**
     * Retrieves checks by status.
     *
     * @param status the check status
     * @return Flux of CheckResponse DTOs
     */
    Flux<CheckResponse> findByStatus(String status);

    /**
     * Retrieves checks by type and status.
     *
     * @param checkType the check type
     * @param status the check status
     * @return Flux of CheckResponse DTOs
     */
    Flux<CheckResponse> findByTypeAndStatus(String checkType, String status);

    /**
     * Retrieves checks by bank account.
     *
     * @param accountId the bank account ID
     * @return Flux of CheckResponse DTOs
     */
    Flux<CheckResponse> findByAccountId(UUID accountId);

    /**
     * Retrieves pending checks due before a specific date.
     *
     * @param date the date threshold
     * @return Flux of CheckResponse DTOs
     */
    Flux<CheckResponse> findPendingChecksDueBefore(LocalDate date);

    /**
     * Creates a new check.
     *
     * @param request the create request DTO
     * @return Mono of created CheckResponse
     */
    Mono<CheckResponse> create(CreateCheckRequest request);

    /**
     * Updates an existing check.
     *
     * @param id the check ID to update
     * @param request the update request DTO
     * @return Mono of updated CheckResponse
     */
    Mono<CheckResponse> update(UUID id, UpdateCheckRequest request);

    /**
     * Deletes a check by its ID.
     *
     * @param id the check ID to delete
     * @return Mono that completes when deletion is done
     */
    Mono<Void> delete(UUID id);

    /**
     * Deposits a check (marks as deposited).
     *
     * @param id the check ID
     * @param depositDate the deposit date
     * @return Mono of updated CheckResponse
     */
    Mono<CheckResponse> deposit(UUID id, LocalDate depositDate);

    /**
     * Cashes a check (marks as cashed).
     *
     * @param id the check ID
     * @param cashedDate the cashed date
     * @return Mono of updated CheckResponse
     */
    Mono<CheckResponse> cash(UUID id, LocalDate cashedDate);

    /**
     * Rejects a check.
     *
     * @param id the check ID
     * @param reason the rejection reason
     * @return Mono of updated CheckResponse
     */
    Mono<CheckResponse> reject(UUID id, String reason);

    /**
     * Cancels a check.
     *
     * @param id the check ID
     * @return Mono of updated CheckResponse
     */
    Mono<CheckResponse> cancel(UUID id);

    /**
     * Emits a check (marks as emitted/handed to beneficiary).
     *
     * @param id the check ID
     * @param emitDate the emit date
     * @return Mono of updated CheckResponse
     */
    Mono<CheckResponse> emit(UUID id, LocalDate emitDate);

    /**
     * Receives a check (marks as received/in possession).
     *
     * @param id the check ID
     * @param receiveDate the receive date
     * @return Mono of updated CheckResponse
     */
    Mono<CheckResponse> receive(UUID id, LocalDate receiveDate);

    /**
     * Marks a check as in processing by the bank.
     *
     * @param id the check ID
     * @return Mono of updated CheckResponse
     */
    Mono<CheckResponse> markAsProcessing(UUID id);

    /**
     * Gets aggregated check statistics.
     *
     * @return Mono of CheckStatsResponse
     */
    Mono<CheckStatsResponse> getStats();

    /**
     * Finds all overdue checks.
     *
     * @return Flux of overdue CheckResponse DTOs
     */
    Flux<CheckResponse> findOverdueChecks();
}
