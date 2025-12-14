package com.rtcomops.treasury.controller;

import com.rtcomops.treasury.dto.request.CreateCheckRequest;
import com.rtcomops.treasury.dto.request.UpdateCheckRequest;
import com.rtcomops.treasury.dto.response.CheckResponse;
import com.rtcomops.treasury.service.CheckService;
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

    private final CheckService checkService;

    public CheckController(CheckService checkService) {
        this.checkService = checkService;
    }

    @GetMapping
    @Operation(summary = "Get all checks")
    public Flux<CheckResponse> getAllChecks() {
        LOG.debug("REST request to get all checks");
        return checkService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get check by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check found"),
        @ApiResponse(responseCode = "404", description = "Check not found")
    })
    public Mono<ResponseEntity<CheckResponse>> getCheckById(@PathVariable UUID id) {
        LOG.debug("REST request to get check by id={}", id);
        return checkService.findById(id).map(ResponseEntity::ok);
    }

    @GetMapping("/type/{checkType}")
    @Operation(summary = "Get checks by type (ISSUED or RECEIVED)")
    public Flux<CheckResponse> getChecksByType(@PathVariable String checkType) {
        LOG.debug("REST request to get checks by type={}", checkType);
        return checkService.findByType(checkType);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get checks by status")
    public Flux<CheckResponse> getChecksByStatus(@PathVariable String status) {
        LOG.debug("REST request to get checks by status={}", status);
        return checkService.findByStatus(status);
    }

    @GetMapping("/type/{checkType}/status/{status}")
    @Operation(summary = "Get checks by type and status")
    public Flux<CheckResponse> getChecksByTypeAndStatus(
            @PathVariable String checkType,
            @PathVariable String status) {
        LOG.debug("REST request to get checks by type={} and status={}", checkType, status);
        return checkService.findByTypeAndStatus(checkType, status);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get checks by bank account")
    public Flux<CheckResponse> getChecksByAccount(@PathVariable UUID accountId) {
        LOG.debug("REST request to get checks by accountId={}", accountId);
        return checkService.findByAccountId(accountId);
    }

    @GetMapping("/pending/due-before")
    @Operation(summary = "Get pending checks due before a date")
    public Flux<CheckResponse> getPendingChecksDueBefore(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LOG.debug("REST request to get pending checks due before {}", date);
        return checkService.findPendingChecksDueBefore(date);
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
        return checkService.create(request)
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
        return checkService.update(id, request).map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a check")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Check deleted"),
        @ApiResponse(responseCode = "404", description = "Check not found")
    })
    public Mono<ResponseEntity<Void>> deleteCheck(@PathVariable UUID id) {
        LOG.debug("REST request to delete check id={}", id);
        return checkService.delete(id)
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
        return checkService.deposit(id, depositDate).map(ResponseEntity::ok);
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
        return checkService.cash(id, cashedDate).map(ResponseEntity::ok);
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
        return checkService.reject(id, reason).map(ResponseEntity::ok);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a check")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check cancelled"),
        @ApiResponse(responseCode = "404", description = "Check not found")
    })
    public Mono<ResponseEntity<CheckResponse>> cancelCheck(@PathVariable UUID id) {
        LOG.debug("REST request to cancel check id={}", id);
        return checkService.cancel(id).map(ResponseEntity::ok);
    }
}
