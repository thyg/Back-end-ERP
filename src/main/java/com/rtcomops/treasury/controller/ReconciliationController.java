package com.rtcomops.treasury.controller;

import com.rtcomops.treasury.dto.request.AutoReconcileRequest;
import com.rtcomops.treasury.dto.request.ReconcileManualRequest;
import com.rtcomops.treasury.dto.response.ReconciliationMatchResponse;
import com.rtcomops.treasury.dto.response.ReconciliationSummaryResponse;
import com.rtcomops.treasury.service.ReconciliationService;
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

import java.util.UUID;

/**
 * REST Controller for Reconciliation operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@RestController
@RequestMapping("/api/reconciliation")
@Tag(name = "Reconciliation", description = "Bank reconciliation endpoints")
public class ReconciliationController {

    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationController.class);

    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @PostMapping("/manual")
    @Operation(summary = "Perform manual reconciliation")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Match created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Statement line or transaction/check not found")
    })
    public Mono<ResponseEntity<ReconciliationMatchResponse>> reconcileManual(
            @Valid @RequestBody ReconcileManualRequest request) {
        LOG.debug("REST request for manual reconciliation of line={}", request.getStatementLineId());
        return reconciliationService.reconcileManual(request)
            .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @PostMapping("/auto")
    @Operation(summary = "Perform automatic reconciliation for a statement")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Auto reconciliation completed"),
        @ApiResponse(responseCode = "404", description = "Statement not found")
    })
    public Flux<ReconciliationMatchResponse> reconcileAuto(
            @Valid @RequestBody AutoReconcileRequest request) {
        LOG.debug("REST request for auto reconciliation of statement={}", request.getBankStatementId());
        return reconciliationService.reconcileAuto(request);
    }

    @DeleteMapping("/match/{matchId}")
    @Operation(summary = "Remove a reconciliation match")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Match removed"),
        @ApiResponse(responseCode = "404", description = "Match not found")
    })
    public Mono<ResponseEntity<Void>> unmatch(@PathVariable UUID matchId) {
        LOG.debug("REST request to remove match id={}", matchId);
        return reconciliationService.unmatch(matchId)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @GetMapping("/summary/{statementId}")
    @Operation(summary = "Get reconciliation summary for a statement")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Summary retrieved"),
        @ApiResponse(responseCode = "404", description = "Statement not found")
    })
    public Mono<ResponseEntity<ReconciliationSummaryResponse>> getSummary(@PathVariable UUID statementId) {
        LOG.debug("REST request to get reconciliation summary for statement={}", statementId);
        return reconciliationService.getSummary(statementId).map(ResponseEntity::ok);
    }

    @GetMapping("/matches/line/{lineId}")
    @Operation(summary = "Get matches for a statement line")
    public Flux<ReconciliationMatchResponse> getMatchesByLine(@PathVariable UUID lineId) {
        LOG.debug("REST request to get matches for line={}", lineId);
        return reconciliationService.findMatchesByLineId(lineId);
    }

    @PostMapping("/check-deposit/{depositId}/line/{lineId}")
    @Operation(
        summary = "Reconcile a check deposit batch with a statement line",
        description = """
            Reconciles a batch check deposit (remise de cheques en lot) with a bank statement line.

            This operation:
            - Changes the deposit status to RECONCILED
            - Creates a single bank transaction for the total deposit amount
            - Updates all checks in the deposit to CASHED status
            - Updates the bank account balance once with the total amount
            - Creates a reconciliation match between the transaction and statement line
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check deposit reconciled successfully"),
        @ApiResponse(responseCode = "400", description = "Deposit already reconciled"),
        @ApiResponse(responseCode = "404", description = "Check deposit or statement line not found")
    })
    public Mono<ResponseEntity<Void>> reconcileCheckDeposit(
            @PathVariable UUID depositId,
            @PathVariable UUID lineId) {
        LOG.debug("REST request to reconcile check deposit id={} with line id={}", depositId, lineId);
        return reconciliationService.reconcileCheckDeposit(depositId, lineId)
            .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }
}
