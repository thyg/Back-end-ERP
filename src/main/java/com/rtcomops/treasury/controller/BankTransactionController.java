package com.rtcomops.treasury.controller;

import com.rtcomops.treasury.dto.request.CreateBankTransactionRequest;
import com.rtcomops.treasury.dto.request.UpdateBankTransactionRequest;
import com.rtcomops.treasury.dto.response.BankTransactionResponse;
import com.rtcomops.treasury.service.BankTransactionService;
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
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@RestController
@RequestMapping("/api/bank-transactions")
@Tag(name = "Bank Transactions", description = "Bank transaction management endpoints")
public class BankTransactionController {

    private static final Logger LOG = LoggerFactory.getLogger(BankTransactionController.class);

    private final BankTransactionService transactionService;

    public BankTransactionController(BankTransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    @Operation(summary = "Get all bank transactions")
    public Flux<BankTransactionResponse> getAllTransactions() {
        LOG.debug("REST request to get all bank transactions");
        return transactionService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bank transaction by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction found"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public Mono<ResponseEntity<BankTransactionResponse>> getTransactionById(@PathVariable UUID id) {
        LOG.debug("REST request to get bank transaction by id={}", id);
        return transactionService.findById(id).map(ResponseEntity::ok);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get transactions by bank account")
    public Flux<BankTransactionResponse> getTransactionsByAccount(@PathVariable UUID accountId) {
        LOG.debug("REST request to get transactions by accountId={}", accountId);
        return transactionService.findByAccountId(accountId);
    }

    @GetMapping("/account/{accountId}/range")
    @Operation(summary = "Get transactions by account and date range")
    public Flux<BankTransactionResponse> getTransactionsByAccountAndDateRange(
            @PathVariable UUID accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LOG.debug("REST request to get transactions by accountId={} from {} to {}", accountId, startDate, endDate);
        return transactionService.findByAccountIdAndDateRange(accountId, startDate, endDate);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get transactions by status")
    public Flux<BankTransactionResponse> getTransactionsByStatus(@PathVariable String status) {
        LOG.debug("REST request to get transactions by status={}", status);
        return transactionService.findByStatus(status);
    }

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
        return transactionService.create(request)
            .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

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
        return transactionService.update(id, request).map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a bank transaction")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Transaction deleted"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public Mono<ResponseEntity<Void>> deleteTransaction(@PathVariable UUID id) {
        LOG.debug("REST request to delete bank transaction id={}", id);
        return transactionService.delete(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @PostMapping("/{id}/validate")
    @Operation(summary = "Validate a bank transaction")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction validated"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public Mono<ResponseEntity<BankTransactionResponse>> validateTransaction(@PathVariable UUID id) {
        LOG.debug("REST request to validate bank transaction id={}", id);
        return transactionService.validate(id).map(ResponseEntity::ok);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a bank transaction")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction cancelled"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public Mono<ResponseEntity<BankTransactionResponse>> cancelTransaction(@PathVariable UUID id) {
        LOG.debug("REST request to cancel bank transaction id={}", id);
        return transactionService.cancel(id).map(ResponseEntity::ok);
    }
}
