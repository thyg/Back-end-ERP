package com.rtcomops.treasury.application.port.in;

import com.rtcomops.treasury.application.dto.request.CreateCheckbookRequest;
import com.rtcomops.treasury.application.dto.response.CheckbookResponse;
import com.rtcomops.treasury.application.dto.response.CheckbookStatsResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Input port for Checkbook use cases.
 *
 * <p>Defines the contract for checkbook business operations.
 * This port is implemented by the domain service and used by the infrastructure layer.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
public interface CheckbookUseCase {

    /**
     * Retrieves all checkbooks.
     *
     * @return Flux of CheckbookResponse DTOs
     */
    Flux<CheckbookResponse> findAll();

    /**
     * Retrieves a checkbook by its ID.
     *
     * @param id the checkbook ID
     * @return Mono of CheckbookResponse
     */
    Mono<CheckbookResponse> findById(UUID id);

    /**
     * Retrieves all checkbooks for a bank account.
     *
     * @param accountId the bank account ID
     * @return Flux of CheckbookResponse DTOs
     */
    Flux<CheckbookResponse> findByBankAccountId(UUID accountId);

    /**
     * Retrieves checkbooks by status.
     *
     * @param status the status to filter by
     * @return Flux of CheckbookResponse DTOs
     */
    Flux<CheckbookResponse> findByStatus(String status);

    /**
     * Creates a new checkbook.
     *
     * @param request the create request DTO
     * @return Mono of created CheckbookResponse
     */
    Mono<CheckbookResponse> create(CreateCheckbookRequest request);

    /**
     * Cancels a checkbook.
     *
     * @param id the checkbook ID
     * @return Mono of updated CheckbookResponse
     */
    Mono<CheckbookResponse> cancel(UUID id);

    /**
     * Gets the next available check number without incrementing the counter.
     * This is for display purposes only.
     *
     * @param id the checkbook ID
     * @return Mono of the next check number string
     */
    Mono<String> peekNextCheckNumber(UUID id);

    /**
     * Gets the active checkbook for a bank account.
     *
     * @param accountId the bank account ID
     * @return Mono of CheckbookResponse, or empty if none active
     */
    Mono<CheckbookResponse> findActiveByBankAccountId(UUID accountId);

    /**
     * Retrieves the system checkbook (type=FICTIF, isSystem=true).
     *
     * @return Mono of CheckbookResponse
     */
    Mono<CheckbookResponse> findSystemCheckbook();

    /**
     * Gets statistics for a specific checkbook.
     *
     * @param checkbookId the checkbook ID
     * @return Mono of CheckbookStatsResponse
     */
    Mono<CheckbookStatsResponse> getStats(UUID checkbookId);
}
