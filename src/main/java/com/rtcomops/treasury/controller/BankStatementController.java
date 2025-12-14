package com.rtcomops.treasury.controller;

import com.rtcomops.treasury.dto.request.CreateBankStatementRequest;
import com.rtcomops.treasury.dto.request.UpdateBankStatementRequest;
import com.rtcomops.treasury.dto.response.BankStatementResponse;
import com.rtcomops.treasury.service.BankStatementService;
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
 * REST Controller for BankStatement operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@RestController
@RequestMapping("/api/bank-statements")
@Tag(name = "Bank Statements", description = "Bank statement management endpoints")
public class BankStatementController {

    private static final Logger LOG = LoggerFactory.getLogger(BankStatementController.class);

    private final BankStatementService statementService;

    public BankStatementController(BankStatementService statementService) {
        this.statementService = statementService;
    }

    @GetMapping
    @Operation(summary = "Get all bank statements")
    public Flux<BankStatementResponse> getAllStatements() {
        LOG.debug("REST request to get all bank statements");
        return statementService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bank statement by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statement found"),
        @ApiResponse(responseCode = "404", description = "Statement not found")
    })
    public Mono<ResponseEntity<BankStatementResponse>> getStatementById(@PathVariable UUID id) {
        LOG.debug("REST request to get bank statement by id={}", id);
        return statementService.findById(id).map(ResponseEntity::ok);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get bank statements by account")
    public Flux<BankStatementResponse> getStatementsByAccount(@PathVariable UUID accountId) {
        LOG.debug("REST request to get bank statements by accountId={}", accountId);
        return statementService.findByAccountId(accountId);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get bank statements by status")
    public Flux<BankStatementResponse> getStatementsByStatus(@PathVariable String status) {
        LOG.debug("REST request to get bank statements by status={}", status);
        return statementService.findByStatus(status);
    }

    @GetMapping("/account/{accountId}/range")
    @Operation(summary = "Get bank statements by account and date range")
    public Flux<BankStatementResponse> getStatementsByAccountAndDateRange(
            @PathVariable UUID accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LOG.debug("REST request to get statements by accountId={} from {} to {}", accountId, startDate, endDate);
        return statementService.findByAccountIdAndDateRange(accountId, startDate, endDate);
    }

    @PostMapping
    @Operation(summary = "Create a new bank statement")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Statement created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Bank account not found"),
        @ApiResponse(responseCode = "409", description = "Duplicate statement for period")
    })
    public Mono<ResponseEntity<BankStatementResponse>> createStatement(
            @Valid @RequestBody CreateBankStatementRequest request) {
        LOG.debug("REST request to create bank statement");
        return statementService.create(request)
            .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a bank statement")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statement updated"),
        @ApiResponse(responseCode = "404", description = "Statement not found")
    })
    public Mono<ResponseEntity<BankStatementResponse>> updateStatement(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBankStatementRequest request) {
        LOG.debug("REST request to update bank statement id={}", id);
        return statementService.update(id, request).map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a bank statement")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Statement deleted"),
        @ApiResponse(responseCode = "404", description = "Statement not found")
    })
    public Mono<ResponseEntity<Void>> deleteStatement(@PathVariable UUID id) {
        LOG.debug("REST request to delete bank statement id={}", id);
        return statementService.delete(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @PostMapping("/{id}/update-totals")
    @Operation(summary = "Recalculate statement totals from lines")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Totals updated"),
        @ApiResponse(responseCode = "404", description = "Statement not found")
    })
    public Mono<ResponseEntity<BankStatementResponse>> updateTotals(@PathVariable UUID id) {
        LOG.debug("REST request to update totals for statement id={}", id);
        return statementService.updateTotals(id).map(ResponseEntity::ok);
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Close a bank statement")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statement closed"),
        @ApiResponse(responseCode = "404", description = "Statement not found")
    })
    public Mono<ResponseEntity<BankStatementResponse>> closeStatement(@PathVariable UUID id) {
        LOG.debug("REST request to close bank statement id={}", id);
        return statementService.close(id).map(ResponseEntity::ok);
    }
}
