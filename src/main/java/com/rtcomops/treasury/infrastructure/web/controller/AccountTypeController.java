package com.rtcomops.treasury.infrastructure.web.controller;

import com.rtcomops.treasury.application.dto.request.CreateAccountTypeRequest;
import com.rtcomops.treasury.application.dto.request.UpdateAccountTypeRequest;
import com.rtcomops.treasury.application.dto.response.AccountSubTypeResponse;
import com.rtcomops.treasury.application.dto.response.AccountTypeResponse;
import com.rtcomops.treasury.application.port.in.AccountTypeUseCase;
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
 * REST Controller for AccountType operations.
 *
 * <p>Provides endpoints for CRUD operations on account types and sub-types.
 * All endpoints are reactive and return Mono or Flux.</p>
 *
 * <p>This controller follows hexagonal architecture by depending on
 * the AccountTypeUseCase port interface rather than a concrete service.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@RestController
@RequestMapping("/api/account-types")
@Tag(name = "Account Types", description = "Account type configuration management endpoints")
public class AccountTypeController {

    private static final Logger LOG = LoggerFactory.getLogger(AccountTypeController.class);

    private final AccountTypeUseCase accountTypeUseCase;

    /**
     * Constructs the AccountTypeController with required dependencies.
     *
     * @param accountTypeUseCase the account type use case port (input port)
     */
    public AccountTypeController(AccountTypeUseCase accountTypeUseCase) {
        this.accountTypeUseCase = accountTypeUseCase;
    }

    /**
     * Retrieves all account types.
     *
     * @param activeOnly if true, returns only active account types
     * @return Flux of AccountTypeResponse
     */
    @GetMapping
    @Operation(
            summary = "Get all account types",
            description = "Retrieves all account types ordered by display order"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account types retrieved successfully")
    })
    public Flux<AccountTypeResponse> getAllAccountTypes(
            @Parameter(description = "Filter to show only active account types")
            @RequestParam(defaultValue = "false") boolean activeOnly) {

        LOG.debug("REST request to get all account types, activeOnly={}", activeOnly);
        return accountTypeUseCase.findAll(activeOnly);
    }

    /**
     * Retrieves an account type by ID.
     *
     * @param id the account type ID
     * @param includeSubTypes if true, includes sub-types in response
     * @return Mono of AccountTypeResponse
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get account type by ID",
            description = "Retrieves a specific account type by its unique identifier"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account type found"),
            @ApiResponse(responseCode = "404", description = "Account type not found")
    })
    public Mono<ResponseEntity<AccountTypeResponse>> getAccountTypeById(
            @Parameter(description = "Account type ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Include sub-types in response")
            @RequestParam(defaultValue = "false") boolean includeSubTypes) {

        LOG.debug("REST request to get account type by id={}, includeSubTypes={}", id, includeSubTypes);

        if (includeSubTypes) {
            return accountTypeUseCase.findByIdWithSubTypes(id)
                    .map(ResponseEntity::ok);
        }
        return accountTypeUseCase.findById(id)
                .map(ResponseEntity::ok);
    }

    /**
     * Retrieves an account type by code.
     *
     * @param code the account type code
     * @return Mono of AccountTypeResponse
     */
    @GetMapping("/code/{code}")
    @Operation(
            summary = "Get account type by code",
            description = "Retrieves a specific account type by its unique code"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account type found"),
            @ApiResponse(responseCode = "404", description = "Account type not found")
    })
    public Mono<ResponseEntity<AccountTypeResponse>> getAccountTypeByCode(
            @Parameter(description = "Account type code (e.g., CHEQUE, ESPECES)", required = true)
            @PathVariable String code) {

        LOG.debug("REST request to get account type by code={}", code);
        return accountTypeUseCase.findByCode(code)
                .map(ResponseEntity::ok);
    }

    /**
     * Retrieves account types that can emit checks.
     *
     * @return Flux of AccountTypeResponse
     */
    @GetMapping("/check-emitters")
    @Operation(
            summary = "Get account types that can emit checks",
            description = "Retrieves all account types where peutEmettreChecques is true"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account types retrieved successfully")
    })
    public Flux<AccountTypeResponse> getCheckEmitters() {
        LOG.debug("REST request to get account types that can emit checks");
        return accountTypeUseCase.findCheckEmitters();
    }

    /**
     * Retrieves account types that can receive checks.
     *
     * @return Flux of AccountTypeResponse
     */
    @GetMapping("/check-receivers")
    @Operation(
            summary = "Get account types that can receive checks",
            description = "Retrieves all account types where peutRecevoirChecques is true"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account types retrieved successfully")
    })
    public Flux<AccountTypeResponse> getCheckReceivers() {
        LOG.debug("REST request to get account types that can receive checks");
        return accountTypeUseCase.findCheckReceivers();
    }

    /**
     * Retrieves account types that allow cash transactions.
     *
     * @return Flux of AccountTypeResponse
     */
    @GetMapping("/cash-enabled")
    @Operation(
            summary = "Get account types that allow cash transactions",
            description = "Retrieves all account types where peutTransactionsEspeces is true"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account types retrieved successfully")
    })
    public Flux<AccountTypeResponse> getCashEnabled() {
        LOG.debug("REST request to get account types that allow cash transactions");
        return accountTypeUseCase.findCashEnabled();
    }

    /**
     * Retrieves account types that allow overdraft.
     *
     * @return Flux of AccountTypeResponse
     */
    @GetMapping("/overdraft-enabled")
    @Operation(
            summary = "Get account types that allow overdraft",
            description = "Retrieves all account types where decouvertAutorise is true"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account types retrieved successfully")
    })
    public Flux<AccountTypeResponse> getOverdraftEnabled() {
        LOG.debug("REST request to get account types with overdraft enabled");
        return accountTypeUseCase.findOverdraftEnabled();
    }

    /**
     * Creates a new account type.
     *
     * @param request the create request
     * @return Mono of created AccountTypeResponse
     */
    @PostMapping
    @Operation(
            summary = "Create a new account type",
            description = "Creates a new account type with the provided configuration"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account type created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Account type with this code already exists")
    })
    public Mono<ResponseEntity<AccountTypeResponse>> createAccountType(
            @Valid @RequestBody CreateAccountTypeRequest request) {

        LOG.debug("REST request to create account type with code={}", request.getCode());
        return accountTypeUseCase.create(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    /**
     * Updates an existing account type.
     *
     * @param id the account type ID to update
     * @param request the update request
     * @return Mono of updated AccountTypeResponse
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Update an account type",
            description = "Updates an existing account type with the provided configuration"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account type updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Account type not found"),
            @ApiResponse(responseCode = "409", description = "Account type with this code already exists")
    })
    public Mono<ResponseEntity<AccountTypeResponse>> updateAccountType(
            @Parameter(description = "Account type ID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAccountTypeRequest request) {

        LOG.debug("REST request to update account type id={}", id);
        return accountTypeUseCase.update(id, request)
                .map(ResponseEntity::ok);
    }

    /**
     * Deletes an account type.
     *
     * @param id the account type ID to delete
     * @return Mono that completes when deletion is done
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete an account type",
            description = "Deletes an account type and all its sub-types by ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Account type deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Account type not found")
    })
    public Mono<ResponseEntity<Void>> deleteAccountType(
            @Parameter(description = "Account type ID", required = true)
            @PathVariable UUID id) {

        LOG.debug("REST request to delete account type id={}", id);
        return accountTypeUseCase.delete(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    /**
     * Retrieves sub-types for a given account type.
     *
     * @param id the parent account type ID
     * @param activeOnly if true, returns only active sub-types
     * @return Flux of AccountSubTypeResponse
     */
    @GetMapping("/{id}/sub-types")
    @Operation(
            summary = "Get sub-types for an account type",
            description = "Retrieves all sub-types belonging to a specific account type"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sub-types retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Account type not found")
    })
    public Flux<AccountSubTypeResponse> getSubTypes(
            @Parameter(description = "Account type ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Filter to show only active sub-types")
            @RequestParam(defaultValue = "false") boolean activeOnly) {

        LOG.debug("REST request to get sub-types for account type id={}, activeOnly={}", id, activeOnly);
        return accountTypeUseCase.findSubTypes(id, activeOnly);
    }
}
