package com.rtcomops.treasury.infrastructure.adapter.incoming.rest;

import com.rtcomops.treasury.application.dto.request.CreateCheckRequest;
import com.rtcomops.treasury.application.dto.request.UpdateCheckRequest;
import com.rtcomops.treasury.application.dto.response.CheckResponse;
import com.rtcomops.treasury.application.dto.response.CheckStatsResponse;
import com.rtcomops.treasury.application.port.in.CheckUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
 * REST Controller for Check operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@RestController
@RequestMapping("/api/checks")
@Tag(name = "Checks", description = "Check management endpoints")
public class CheckController {

    private static final Logger LOG = LoggerFactory.getLogger(CheckController.class);

    private final CheckUseCase checkUseCase;

    public CheckController(CheckUseCase checkUseCase) {
        this.checkUseCase = checkUseCase;
    }

    @GetMapping
    @Operation(summary = "Get all checks with optional filters")
    public Flux<CheckResponse> getAllChecks(
            @Parameter(description = "Filter checks by a specific checkbook ID")
            @RequestParam(required = false) UUID checkbookId) {
        LOG.debug("REST request to get all checks with checkbookId={}", checkbookId);
        return checkUseCase.findAll(checkbookId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get check by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check found"),
        @ApiResponse(responseCode = "404", description = "Check not found")
    })
    public Mono<ResponseEntity<CheckResponse>> getCheckById(@PathVariable UUID id) {
        LOG.debug("REST request to get check by id={}", id);
        return checkUseCase.findById(id).map(ResponseEntity::ok);
    }

    @GetMapping("/type/{checkType}")
    @Operation(summary = "Get checks by type (ISSUED or RECEIVED)")
    public Flux<CheckResponse> getChecksByType(@PathVariable String checkType) {
        LOG.debug("REST request to get checks by type={}", checkType);
        return checkUseCase.findByType(checkType);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get checks by status")
    public Flux<CheckResponse> getChecksByStatus(@PathVariable String status) {
        LOG.debug("REST request to get checks by status={}", status);
        return checkUseCase.findByStatus(status);
    }

    @GetMapping("/type/{checkType}/status/{status}")
    @Operation(summary = "Get checks by type and status")
    public Flux<CheckResponse> getChecksByTypeAndStatus(
            @PathVariable String checkType,
            @PathVariable String status) {
        LOG.debug("REST request to get checks by type={} and status={}", checkType, status);
        return checkUseCase.findByTypeAndStatus(checkType, status);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get checks by bank account")
    public Flux<CheckResponse> getChecksByAccount(@PathVariable UUID accountId) {
        LOG.debug("REST request to get checks by accountId={}", accountId);
        return checkUseCase.findByAccountId(accountId);
    }

    @GetMapping("/pending/due-before")
    @Operation(summary = "Get pending checks due before a date")
    public Flux<CheckResponse> getPendingChecksDueBefore(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LOG.debug("REST request to get pending checks due before {}", date);
        return checkUseCase.findPendingChecksDueBefore(date);
    }

    @PostMapping
    @Operation(summary = "Create a new check")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Check created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Bank account not found"),
        @ApiResponse(responseCode = "409", description = "Duplicate check number")
    })
    public Mono<ResponseEntity<CheckResponse>> createCheck(
            @Valid @RequestBody CreateCheckRequest request) {
        LOG.debug("REST request to create check");
        return checkUseCase.create(request)
            .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a check")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check updated"),
        @ApiResponse(responseCode = "404", description = "Check not found"),
        @ApiResponse(responseCode = "409", description = "Duplicate check number")
    })
    public Mono<ResponseEntity<CheckResponse>> updateCheck(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCheckRequest request) {
        LOG.debug("REST request to update check id={}", id);
        return checkUseCase.update(id, request).map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a check")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Check deleted"),
        @ApiResponse(responseCode = "404", description = "Check not found")
    })
    public Mono<ResponseEntity<Void>> deleteCheck(@PathVariable UUID id) {
        LOG.debug("REST request to delete check id={}", id);
        return checkUseCase.delete(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @PostMapping("/{id}/deposit")
    @Operation(summary = "Mark check as deposited")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check deposited"),
        @ApiResponse(responseCode = "404", description = "Check not found")
    })
    public Mono<ResponseEntity<CheckResponse>> depositCheck(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate depositDate) {
        LOG.debug("REST request to deposit check id={} on {}", id, depositDate);
        return checkUseCase.deposit(id, depositDate).map(ResponseEntity::ok);
    }

    @PostMapping("/{id}/cash")
    @Operation(summary = "Mark check as cashed")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check cashed"),
        @ApiResponse(responseCode = "404", description = "Check not found")
    })
    public Mono<ResponseEntity<CheckResponse>> cashCheck(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate cashedDate) {
        LOG.debug("REST request to cash check id={} on {}", id, cashedDate);
        return checkUseCase.cash(id, cashedDate).map(ResponseEntity::ok);
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Mark check as rejected")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check rejected"),
        @ApiResponse(responseCode = "404", description = "Check not found")
    })
    public Mono<ResponseEntity<CheckResponse>> rejectCheck(
            @PathVariable UUID id,
            @RequestParam String reason) {
        LOG.debug("REST request to reject check id={}, reason={}", id, reason);
        return checkUseCase.reject(id, reason).map(ResponseEntity::ok);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a check")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check cancelled"),
        @ApiResponse(responseCode = "404", description = "Check not found")
    })
    public Mono<ResponseEntity<CheckResponse>> cancelCheck(@PathVariable UUID id) {
        LOG.debug("REST request to cancel check id={}", id);
        return checkUseCase.cancel(id).map(ResponseEntity::ok);
    }

    @PostMapping("/{id}/emit")
    @Operation(summary = "Mark check as emitted (handed to beneficiary)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check emitted"),
        @ApiResponse(responseCode = "400", description = "Invalid state transition"),
        @ApiResponse(responseCode = "404", description = "Check not found")
    })
    public Mono<ResponseEntity<CheckResponse>> emitCheck(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate emitDate) {
        LOG.debug("REST request to emit check id={}", id);
        LocalDate effectiveDate = emitDate != null ? emitDate : LocalDate.now();
        return checkUseCase.emit(id, effectiveDate).map(ResponseEntity::ok);
    }

    @PostMapping("/{id}/receive")
    @Operation(summary = "Mark check as received (in our possession)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check marked as received"),
        @ApiResponse(responseCode = "400", description = "Invalid state transition"),
        @ApiResponse(responseCode = "404", description = "Check not found")
    })
    public Mono<ResponseEntity<CheckResponse>> receiveCheck(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate receiveDate) {
        LOG.debug("REST request to receive check id={}", id);
        LocalDate effectiveDate = receiveDate != null ? receiveDate : LocalDate.now();
        return checkUseCase.receive(id, effectiveDate).map(ResponseEntity::ok);
    }

    @PostMapping("/{id}/processing")
    @Operation(summary = "Mark a check as in progress")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check marked as in progress"),
        @ApiResponse(responseCode = "400", description = "Invalid state transition"),
        @ApiResponse(responseCode = "404", description = "Check not found")
    })
    public Mono<ResponseEntity<CheckResponse>> markAsProcessing(@PathVariable UUID id) {
        LOG.debug("REST request to mark check id={} as in progress", id);
        return checkUseCase.markAsProcessing(id).map(ResponseEntity::ok);
    }

    // =========================================================================
    // STATISTIQUES
    // =========================================================================

    @GetMapping("/stats")
    @Operation(summary = "Get aggregated check statistics",
               description = "Returns KPIs including counts and amounts by status, overdue checks, and totals by type")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    public Mono<ResponseEntity<CheckStatsResponse>> getStats() {
        LOG.debug("REST request to get check statistics");
        return checkUseCase.getStats().map(ResponseEntity::ok);
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get overdue checks",
               description = "Returns all received checks with due date in the past that are not yet cashed/rejected/cancelled")
    @ApiResponse(responseCode = "200", description = "Overdue checks retrieved")
    public Flux<CheckResponse> getOverdueChecks() {
        LOG.debug("REST request to get overdue checks");
        return checkUseCase.findOverdueChecks();
    }
}
