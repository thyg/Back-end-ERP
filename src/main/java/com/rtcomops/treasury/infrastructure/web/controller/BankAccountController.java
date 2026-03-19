package com.rtcomops.treasury.infrastructure.web.controller;

import com.rtcomops.treasury.application.dto.request.CreateBankAccountRequest;
import com.rtcomops.treasury.application.dto.request.UpdateBankAccountRequest;
import com.rtcomops.treasury.application.dto.response.BankAccountResponse;
import com.rtcomops.treasury.application.port.in.BankAccountUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST Controller for BankAccount operations.
 *
 * <p>Provides endpoints for CRUD operations on bank accounts.
 * All endpoints are reactive and return Mono or Flux.</p>
 *
 * <p>This controller follows hexagonal architecture by depending on
 * the BankAccountUseCase port interface rather than a concrete service.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@RestController
@RequestMapping("/api/bank-accounts")
@Tag(name = "Bank Accounts", description = "Bank account management endpoints")
public class BankAccountController {

    private static final Logger LOG = LoggerFactory.getLogger(BankAccountController.class);

    private final BankAccountUseCase bankAccountUseCase;

    /**
     * Constructs the BankAccountController with required dependencies.
     *
     * @param bankAccountUseCase the bank account use case port (input port)
     */
    public BankAccountController(BankAccountUseCase bankAccountUseCase) {
        this.bankAccountUseCase = bankAccountUseCase;
    }

    /**
     * Retrieves all bank accounts.
     *
     * @param activeOnly if true, returns only active bank accounts
     * @return Flux of BankAccountResponse
     */
    @GetMapping
    @Operation(
            summary = "Get all bank accounts",
            description = "Retrieves all bank accounts ordered by name"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bank accounts retrieved successfully")
    })
    public Flux<BankAccountResponse> getAllBankAccounts(
            @Parameter(description = "Filter to show only active bank accounts")
            @RequestParam(defaultValue = "false") boolean activeOnly) {

        LOG.debug("REST request to get all bank accounts, activeOnly={}", activeOnly);
        return bankAccountUseCase.findAll(activeOnly);
    }

    /**
     * Retrieves a bank account by ID.
     *
     * @param id the bank account ID
     * @return Mono of BankAccountResponse
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get bank account by ID",
            description = "Retrieves a specific bank account by its unique identifier"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bank account found"),
            @ApiResponse(responseCode = "404", description = "Bank account not found")
    })
    public Mono<ResponseEntity<BankAccountResponse>> getBankAccountById(
            @Parameter(description = "Bank account ID", required = true)
            @PathVariable UUID id) {

        LOG.debug("REST request to get bank account by id={}", id);
        return bankAccountUseCase.findById(id)
                .map(ResponseEntity::ok);
    }

    /**
     * Retrieves bank accounts by bank ID.
     *
     * @param bankId the bank ID
     * @return Flux of BankAccountResponse
     */
    @GetMapping("/bank/{bankId}")
    @Operation(
            summary = "Get bank accounts by bank ID",
            description = "Retrieves all bank accounts belonging to a specific bank"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bank accounts retrieved successfully")
    })
    public Flux<BankAccountResponse> getBankAccountsByBankId(
            @Parameter(description = "Bank ID", required = true)
            @PathVariable UUID bankId) {

        LOG.debug("REST request to get bank accounts by bankId={}", bankId);
        return bankAccountUseCase.findByBankId(bankId);
    }

    /**
     * Creates a new bank account.
     *
     * @param request the create request
     * @return Mono of created BankAccountResponse
     */
    @PostMapping
    @Operation(
            summary = "Create a new bank account",
            description = "Creates a new bank account with the provided configuration"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bank account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Duplicate account number or IBAN")
    })
    public Mono<ResponseEntity<BankAccountResponse>> createBankAccount(
            @Valid @RequestBody CreateBankAccountRequest request) {

        LOG.debug("REST request to create bank account with name={}", request.getName());
        return bankAccountUseCase.create(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    /**
     * Updates an existing bank account.
     *
     * @param id the bank account ID to update
     * @param request the update request
     * @return Mono of updated BankAccountResponse
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Update a bank account",
            description = "Updates an existing bank account with the provided configuration"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bank account updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Bank account not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate account number or IBAN")
    })
    public Mono<ResponseEntity<BankAccountResponse>> updateBankAccount(
            @Parameter(description = "Bank account ID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBankAccountRequest request) {

        LOG.debug("REST request to update bank account id={}", id);
        return bankAccountUseCase.update(id, request)
                .map(ResponseEntity::ok);
    }

    /**
     * Deletes a bank account.
     *
     * @param id the bank account ID to delete
     * @return Mono that completes when deletion is done
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a bank account",
            description = "Deletes a bank account by ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Bank account deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Bank account not found")
    })
    public Mono<ResponseEntity<Void>> deleteBankAccount(
            @Parameter(description = "Bank account ID", required = true)
            @PathVariable UUID id) {

        LOG.debug("REST request to delete bank account id={}", id);
        return bankAccountUseCase.delete(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
