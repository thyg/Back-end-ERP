package com.rtcomops.treasury.infrastructure.adapter.incoming.rest;

import com.rtcomops.treasury.application.dto.request.CreateBankTransactionRequest;
import com.rtcomops.treasury.application.dto.request.UpdateBankTransactionRequest;
import com.rtcomops.treasury.application.dto.response.BankTransactionResponse;
import com.rtcomops.treasury.application.port.in.BankTransactionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * REST Controller for BankTransaction operations.
 *
 * <p>This controller provides REST endpoints for managing bank transactions.
 * It delegates all business logic to the BankTransactionUseCase port.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@RestController
@RequestMapping("/api/bank-transactions")
@Tag(name = "Bank Transactions", description = "Bank transaction management endpoints")
public class BankTransactionController {

    private static final Logger LOG = LoggerFactory.getLogger(BankTransactionController.class);

    private final BankTransactionUseCase transactionUseCase;

    /**
     * Constructs the controller with the required use case port.
     *
     * @param transactionUseCase the bank transaction use case port
     */
    public BankTransactionController(BankTransactionUseCase transactionUseCase) {
        this.transactionUseCase = transactionUseCase;
    }

    /**
     * Gets all bank transactions.
     *
     * @return Flux of all bank transactions
     */
    @GetMapping
    @Operation(summary = "Get all bank transactions")
    public Flux<BankTransactionResponse> getAllTransactions() {
        LOG.debug("REST request to get all bank transactions");
        return transactionUseCase.findAll();
    }

    /**
     * Gets a bank transaction by ID.
     *
     * @param id the transaction ID
     * @return the transaction response
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get bank transaction by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction found"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public Mono<ResponseEntity<BankTransactionResponse>> getTransactionById(@PathVariable UUID id) {
        LOG.debug("REST request to get bank transaction by id={}", id);
        return transactionUseCase.findById(id).map(ResponseEntity::ok);
    }

    /**
     * Gets transactions by bank account.
     *
     * @param accountId the bank account ID
     * @return Flux of transactions for the account
     */
    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get transactions by bank account")
    public Flux<BankTransactionResponse> getTransactionsByAccount(@PathVariable UUID accountId) {
        LOG.debug("REST request to get transactions by accountId={}", accountId);
        return transactionUseCase.findByAccountId(accountId);
    }

    /**
     * Gets transactions by account and date range.
     *
     * @param accountId the bank account ID
     * @param startDate the start date
     * @param endDate the end date
     * @return Flux of transactions in the date range
     */
    @GetMapping("/account/{accountId}/range")
    @Operation(summary = "Get transactions by account and date range")
    public Flux<BankTransactionResponse> getTransactionsByAccountAndDateRange(
            @PathVariable UUID accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LOG.debug("REST request to get transactions by accountId={} from {} to {}", accountId, startDate, endDate);
        return transactionUseCase.findByAccountIdAndDateRange(accountId, startDate, endDate);
    }

    /**
     * Gets transactions by status.
     *
     * @param status the transaction status
     * @return Flux of transactions with the given status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get transactions by status")
    public Flux<BankTransactionResponse> getTransactionsByStatus(@PathVariable String status) {
        LOG.debug("REST request to get transactions by status={}", status);
        return transactionUseCase.findByStatus(status);
    }

    /**
     * Creates a new bank transaction.
     *
     * @param request the creation request
     * @return the created transaction response
     */
    @PostMapping
    @Operation(summary = "Create a new bank transaction")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Transaction created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Account or transaction type not found")
    })
    public Mono<ResponseEntity<BankTransactionResponse>> createTransaction(
            @Valid @RequestBody CreateBankTransactionRequest request) {
        LOG.debug("REST request to create bank transaction");
        return transactionUseCase.create(request)
            .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    /**
     * Updates a bank transaction.
     *
     * @param id the transaction ID
     * @param request the update request
     * @return the updated transaction response
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a bank transaction")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction updated"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public Mono<ResponseEntity<BankTransactionResponse>> updateTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBankTransactionRequest request) {
        LOG.debug("REST request to update bank transaction id={}", id);
        return transactionUseCase.update(id, request).map(ResponseEntity::ok);
    }

    /**
     * Deletes a bank transaction.
     *
     * @param id the transaction ID
     * @return empty response
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a bank transaction")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Transaction deleted"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public Mono<ResponseEntity<Void>> deleteTransaction(@PathVariable UUID id) {
        LOG.debug("REST request to delete bank transaction id={}", id);
        return transactionUseCase.delete(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    /**
     * Validates a bank transaction.
     *
     * @param id the transaction ID
     * @return the validated transaction response
     */
    @PostMapping("/{id}/validate")
    @Operation(summary = "Validate a bank transaction")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction validated"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public Mono<ResponseEntity<BankTransactionResponse>> validateTransaction(@PathVariable UUID id) {
        LOG.debug("REST request to validate bank transaction id={}", id);
        return transactionUseCase.validate(id).map(ResponseEntity::ok);
    }

    /**
     * Cancels a bank transaction.
     *
     * @param id the transaction ID
     * @return the cancelled transaction response
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a bank transaction")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction cancelled"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public Mono<ResponseEntity<BankTransactionResponse>> cancelTransaction(@PathVariable UUID id) {
        LOG.debug("REST request to cancel bank transaction id={}", id);
        return transactionUseCase.cancel(id).map(ResponseEntity::ok);
    }
}
