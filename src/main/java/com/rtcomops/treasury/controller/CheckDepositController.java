package com.rtcomops.treasury.controller;

import com.rtcomops.treasury.dto.request.CreateCheckDepositRequest;
import com.rtcomops.treasury.dto.response.CheckDepositResponse;
import com.rtcomops.treasury.service.CheckDepositService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
 * REST Controller for CheckDeposit operations.
 *
 * <p>Manages batch check deposits (remises de cheques en lot) with a 3-step workflow:</p>
 * <ol>
 *   <li><strong>Create</strong> (POST /api/check-deposits) - Creates deposit with PENDING status</li>
 *   <li><strong>Confirm Deposit</strong> (POST /api/check-deposits/{id}/confirm-deposit) - Moves to DEPOSITED</li>
 *   <li><strong>Cash</strong> (POST /api/check-deposits/{id}/cash) - Moves to CASHED, creates transaction</li>
 * </ol>
 *
 * @author RT-ComOps Team
 * @version 2.0.0
 * @since 2026-02-16
 */
@RestController
@RequestMapping("/api/check-deposits")
@Tag(name = "Check Deposits", description = "Batch check deposit management endpoints (remises en lot)")
public class CheckDepositController {

    private static final Logger LOG = LoggerFactory.getLogger(CheckDepositController.class);

    private final CheckDepositService depositService;

    public CheckDepositController(CheckDepositService depositService) {
        this.depositService = depositService;
    }

    // =========================================================================
    // QUERY ENDPOINTS
    // =========================================================================

    @GetMapping
    @Operation(
        summary = "Get all check deposits",
        description = "Retrieves all batch check deposits, ordered by deposit date (most recent first)")
    @ApiResponse(responseCode = "200", description = "List of check deposits retrieved")
    public Flux<CheckDepositResponse> getAllDeposits() {
        LOG.debug("REST request to get all check deposits");
        return depositService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get check deposit by ID",
        description = "Retrieves a specific check deposit with full details including the list of checks")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check deposit found"),
        @ApiResponse(responseCode = "404", description = "Check deposit not found")
    })
    public Mono<ResponseEntity<CheckDepositResponse>> getDepositById(@PathVariable UUID id) {
        LOG.debug("REST request to get check deposit by id={}", id);
        return depositService.findById(id).map(ResponseEntity::ok);
    }

    @GetMapping("/account/{accountId}")
    @Operation(
        summary = "Get check deposits by bank account",
        description = "Retrieves all check deposits for a specific bank account")
    @ApiResponse(responseCode = "200", description = "List of check deposits retrieved")
    public Flux<CheckDepositResponse> getDepositsByAccount(
            @Parameter(description = "Bank account ID")
            @PathVariable UUID accountId) {
        LOG.debug("REST request to get check deposits for account={}", accountId);
        return depositService.findByBankAccountId(accountId);
    }

    @GetMapping("/unreconciled")
    @Operation(
        summary = "Get unreconciled check deposits",
        description = "Retrieves all check deposits with status PENDING or DEPOSITED (not yet cashed)")
    @ApiResponse(responseCode = "200", description = "List of unreconciled deposits retrieved")
    public Flux<CheckDepositResponse> getUnreconciledDeposits() {
        LOG.debug("REST request to get unreconciled check deposits");
        return depositService.findUnreconciled();
    }

    // =========================================================================
    // WORKFLOW STEP 1: CREATE (PENDING)
    // =========================================================================

    @PostMapping
    @Operation(
        summary = "Create a new check deposit batch (Step 1)",
        description = """
            Creates a new batch check deposit (remise en lot) with status PENDING.

            Workflow Step 1: The deposit is created and checks are assigned to it,
            but their status remains RECEIVED. They become unavailable for other deposits.

            Validations:
            - All checks must exist
            - All checks must have status RECEIVED
            - All checks must not already be assigned to another deposit
            - All checks must belong to the specified bank account

            Upon creation:
            - A unique reference is generated (e.g., REM-202602-0001)
            - The deposit is created with status PENDING
            - Checks are assigned to the deposit (checkDepositId set)
            - Check statuses remain RECEIVED
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Check deposit created successfully with PENDING status"),
        @ApiResponse(responseCode = "400", description = "Invalid request (validation failed)"),
        @ApiResponse(responseCode = "404", description = "Bank account or one of the checks not found")
    })
    public Mono<ResponseEntity<CheckDepositResponse>> createDeposit(
            @Valid @RequestBody CreateCheckDepositRequest request) {
        LOG.debug("REST request to create check deposit with {} checks for account={}",
            request.getCheckIds().size(), request.getBankAccountId());
        return depositService.createDeposit(request)
            .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    // =========================================================================
    // WORKFLOW STEP 2: CONFIRM DEPOSIT (DEPOSITED)
    // =========================================================================

    @PostMapping("/{id}/confirm-deposit")
    @Operation(
        summary = "Confirm bank deposit (Step 2)",
        description = """
            Confirms that the deposit has been physically deposited at the bank.

            Workflow Step 2: The deposit transitions from PENDING to DEPOSITED,
            and all linked checks transition to DEPOSITED status.

            Validations:
            - Deposit must be in PENDING status
            - Deposit date must not be earlier than the most recent receiptDate among the checks
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Deposit confirmed, status changed to DEPOSITED"),
        @ApiResponse(responseCode = "400", description = "Invalid request (wrong status or invalid date)"),
        @ApiResponse(responseCode = "404", description = "Check deposit not found")
    })
    public Mono<ResponseEntity<CheckDepositResponse>> confirmDeposit(
            @Parameter(description = "Check deposit ID")
            @PathVariable UUID id,
            @Parameter(description = "Date when the deposit was made at the bank (YYYY-MM-DD)")
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate depositDate) {
        LOG.debug("REST request to confirm deposit id={} with depositDate={}", id, depositDate);
        return depositService.confirmDeposit(id, depositDate)
            .map(ResponseEntity::ok);
    }

    // =========================================================================
    // WORKFLOW STEP 3: CASH (CASHED)
    // =========================================================================

    @PostMapping("/{id}/cash")
    @Operation(
        summary = "Mark deposit as cashed (Step 3)",
        description = """
            Marks the deposit as cashed (funds received in account).

            Workflow Step 3: The deposit transitions from DEPOSITED to CASHED.
            A single BankTransaction is created for the total amount, the account
            balance is updated, and all linked checks transition to CASHED status.

            Validations:
            - Deposit must be in DEPOSITED status
            - Cashed date must not be earlier than deposit date
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Deposit cashed, transaction created, status changed to CASHED"),
        @ApiResponse(responseCode = "400", description = "Invalid request (wrong status or invalid date)"),
        @ApiResponse(responseCode = "404", description = "Check deposit not found")
    })
    public Mono<ResponseEntity<CheckDepositResponse>> cashDeposit(
            @Parameter(description = "Check deposit ID")
            @PathVariable UUID id,
            @Parameter(description = "Date when the funds were received (YYYY-MM-DD)")
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate cashedDate) {
        LOG.debug("REST request to cash deposit id={} with cashedDate={}", id, cashedDate);
        return depositService.cashDeposit(id, cashedDate)
            .map(ResponseEntity::ok);
    }

    // =========================================================================
    // CANCEL DEPOSIT
    // =========================================================================

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Cancel a pending deposit",
        description = """
            Cancels a PENDING deposit and releases the assigned checks.

            Only deposits in PENDING status can be cancelled.
            All assigned checks will have their checkDepositId cleared and return to being available.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deposit cancelled successfully"),
        @ApiResponse(responseCode = "400", description = "Deposit is not in PENDING status"),
        @ApiResponse(responseCode = "404", description = "Check deposit not found")
    })
    public Mono<ResponseEntity<Void>> cancelDeposit(
            @Parameter(description = "Check deposit ID")
            @PathVariable UUID id) {
        LOG.debug("REST request to cancel deposit id={}", id);
        return depositService.cancelDeposit(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
