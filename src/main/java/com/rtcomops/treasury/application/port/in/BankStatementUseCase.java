package com.rtcomops.treasury.application.port.in;

import com.rtcomops.treasury.application.dto.request.CreateBankStatementRequest;
import com.rtcomops.treasury.application.dto.request.UpdateBankStatementRequest;
import com.rtcomops.treasury.application.dto.response.BankStatementResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Input port for BankStatement use cases.
 *
 * <p>Defines the contract for bank statement business operations.
 * This port is implemented by the domain service and used by the infrastructure layer.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
public interface BankStatementUseCase {

    /**
     * Retrieves all bank statements ordered by date descending.
     *
     * @return Flux of BankStatementResponse DTOs
     */
    Flux<BankStatementResponse> findAll();

    /**
     * Retrieves a bank statement by its ID.
     *
     * @param id the statement ID
     * @return Mono of BankStatementResponse
     */
    Mono<BankStatementResponse> findById(UUID id);

    /**
     * Retrieves bank statements by bank account ID.
     *
     * @param accountId the bank account ID
     * @return Flux of BankStatementResponse DTOs
     */
    Flux<BankStatementResponse> findByAccountId(UUID accountId);

    /**
     * Retrieves bank statements by status.
     *
     * @param status the statement status
     * @return Flux of BankStatementResponse DTOs
     */
    Flux<BankStatementResponse> findByStatus(String status);

    /**
     * Retrieves bank statements by account ID and date range.
     *
     * @param accountId the bank account ID
     * @param startDate the start date
     * @param endDate the end date
     * @return Flux of BankStatementResponse DTOs
     */
    Flux<BankStatementResponse> findByAccountIdAndDateRange(UUID accountId, LocalDate startDate, LocalDate endDate);

    /**
     * Creates a new bank statement.
     *
     * @param request the create request DTO
     * @return Mono of created BankStatementResponse
     */
    Mono<BankStatementResponse> create(CreateBankStatementRequest request);

    /**
     * Updates an existing bank statement.
     *
     * @param id the statement ID to update
     * @param request the update request DTO
     * @return Mono of updated BankStatementResponse
     */
    Mono<BankStatementResponse> update(UUID id, UpdateBankStatementRequest request);

    /**
     * Deletes a bank statement and all its lines.
     *
     * @param id the statement ID to delete
     * @return Mono that completes when deletion is done
     */
    Mono<Void> delete(UUID id);

    /**
     * Updates the statement totals and line count.
     *
     * @param id the statement ID
     * @return Mono of updated BankStatementResponse
     */
    Mono<BankStatementResponse> updateTotals(UUID id);

    /**
     * Closes a bank statement after reconciliation is complete.
     *
     * @param id the statement ID
     * @return Mono of closed BankStatementResponse
     */
    Mono<BankStatementResponse> close(UUID id);
}
