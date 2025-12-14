package com.rtcomops.treasury.controller;

import com.rtcomops.treasury.dto.request.CreateBankAccountRequest;
import com.rtcomops.treasury.dto.request.UpdateBankAccountRequest;
import com.rtcomops.treasury.dto.response.BankAccountResponse;
import com.rtcomops.treasury.service.BankAccountService;
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
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST Controller for BankAccount operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@RestController
@RequestMapping("/api/bank-accounts")
@Tag(name = "Bank Accounts", description = "Bank account management endpoints")
public class BankAccountController {

    private static final Logger LOG = LoggerFactory.getLogger(BankAccountController.class);

    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @GetMapping
    @Operation(summary = "Get all bank accounts")
    public Flux<BankAccountResponse> getAllBankAccounts(
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        LOG.debug("REST request to get all bank accounts, activeOnly={}", activeOnly);
        return bankAccountService.findAll(activeOnly);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bank account by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bank account found"),
        @ApiResponse(responseCode = "404", description = "Bank account not found")
    })
    public Mono<ResponseEntity<BankAccountResponse>> getBankAccountById(@PathVariable UUID id) {
        LOG.debug("REST request to get bank account by id={}", id);
        return bankAccountService.findById(id).map(ResponseEntity::ok);
    }

    @GetMapping("/bank/{bankId}")
    @Operation(summary = "Get bank accounts by bank ID")
    public Flux<BankAccountResponse> getBankAccountsByBankId(@PathVariable UUID bankId) {
        LOG.debug("REST request to get bank accounts by bankId={}", bankId);
        return bankAccountService.findByBankId(bankId);
    }

    @PostMapping
    @Operation(summary = "Create a new bank account")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Bank account created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "409", description = "Duplicate account number or IBAN")
    })
    public Mono<ResponseEntity<BankAccountResponse>> createBankAccount(
            @Valid @RequestBody CreateBankAccountRequest request) {
        LOG.debug("REST request to create bank account");
        return bankAccountService.create(request)
            .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a bank account")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bank account updated"),
        @ApiResponse(responseCode = "404", description = "Bank account not found"),
        @ApiResponse(responseCode = "409", description = "Duplicate account number or IBAN")
    })
    public Mono<ResponseEntity<BankAccountResponse>> updateBankAccount(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBankAccountRequest request) {
        LOG.debug("REST request to update bank account id={}", id);
        return bankAccountService.update(id, request).map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a bank account")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Bank account deleted"),
        @ApiResponse(responseCode = "404", description = "Bank account not found")
    })
    public Mono<ResponseEntity<Void>> deleteBankAccount(@PathVariable UUID id) {
        LOG.debug("REST request to delete bank account id={}", id);
        return bankAccountService.delete(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
