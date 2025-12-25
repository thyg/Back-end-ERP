package com.rtcomops.treasury.controller;

import com.rtcomops.treasury.dto.request.CreateCheckbookRequest;
import com.rtcomops.treasury.dto.response.CheckbookResponse;
import com.rtcomops.treasury.service.CheckbookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Checkbook operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
@RestController
@RequestMapping("/api/checkbooks")
@Tag(name = "Checkbooks", description = "Checkbook (chéquier) management endpoints")
public class CheckbookController {

    private static final Logger LOG = LoggerFactory.getLogger(CheckbookController.class);

    private final CheckbookService checkbookService;

    public CheckbookController(CheckbookService checkbookService) {
        this.checkbookService = checkbookService;
    }

    @GetMapping
    @Operation(summary = "Get all checkbooks")
    public Flux<CheckbookResponse> getAllCheckbooks() {
        LOG.debug("REST request to get all checkbooks");
        return checkbookService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get checkbook by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Checkbook found"),
        @ApiResponse(responseCode = "404", description = "Checkbook not found")
    })
    public Mono<ResponseEntity<CheckbookResponse>> getCheckbookById(@PathVariable UUID id) {
        LOG.debug("REST request to get checkbook by id={}", id);
        return checkbookService.findById(id).map(ResponseEntity::ok);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get checkbooks by bank account")
    public Flux<CheckbookResponse> getCheckbooksByAccount(@PathVariable UUID accountId) {
        LOG.debug("REST request to get checkbooks by accountId={}", accountId);
        return checkbookService.findByBankAccountId(accountId);
    }

    @GetMapping("/account/{accountId}/active")
    @Operation(summary = "Get active checkbook for a bank account")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Active checkbook found"),
        @ApiResponse(responseCode = "404", description = "No active checkbook")
    })
    public Mono<ResponseEntity<CheckbookResponse>> getActiveCheckbook(@PathVariable UUID accountId) {
        LOG.debug("REST request to get active checkbook for accountId={}", accountId);
        return checkbookService.findActiveByBankAccountId(accountId)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get checkbooks by status")
    public Flux<CheckbookResponse> getCheckbooksByStatus(@PathVariable String status) {
        LOG.debug("REST request to get checkbooks by status={}", status);
        return checkbookService.findByStatus(status);
    }

    @PostMapping
    @Operation(summary = "Create a new checkbook")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Checkbook created"),
        @ApiResponse(responseCode = "400", description = "Invalid request or overlapping range"),
        @ApiResponse(responseCode = "404", description = "Bank account not found")
    })
    public Mono<ResponseEntity<CheckbookResponse>> createCheckbook(
            @Valid @RequestBody CreateCheckbookRequest request) {
        LOG.debug("REST request to create checkbook");
        return checkbookService.create(request)
            .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a checkbook")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Checkbook cancelled"),
        @ApiResponse(responseCode = "400", description = "Cannot cancel (already cancelled or finished)"),
        @ApiResponse(responseCode = "404", description = "Checkbook not found")
    })
    public Mono<ResponseEntity<CheckbookResponse>> cancelCheckbook(@PathVariable UUID id) {
        LOG.debug("REST request to cancel checkbook id={}", id);
        return checkbookService.cancel(id).map(ResponseEntity::ok);
    }

    @GetMapping("/{id}/next-number")
    @Operation(summary = "Get and allocate next check number")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Next check number allocated"),
        @ApiResponse(responseCode = "400", description = "No checks available"),
        @ApiResponse(responseCode = "404", description = "Checkbook not found")
    })
    public Mono<ResponseEntity<Map<String, String>>> getNextCheckNumber(@PathVariable UUID id) {
        LOG.debug("REST request to get next check number from checkbook id={}", id);
        return checkbookService.getNextCheckNumber(id)
            .map(number -> ResponseEntity.ok(Map.of(
                "checkbookId", id.toString(),
                "checkNumber", number
            )));
    }
}
