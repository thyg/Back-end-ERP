package com.rtcomops.treasury.infrastructure.web.controller;

import com.rtcomops.treasury.application.dto.request.CreateTransactionTypeRequest;
import com.rtcomops.treasury.application.dto.request.UpdateTransactionTypeRequest;
import com.rtcomops.treasury.application.dto.response.TransactionTypeResponse;
import com.rtcomops.treasury.application.port.in.TransactionTypeUseCase;
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
 * REST Controller for TransactionType operations.
 *
 * <p>Provides endpoints for CRUD operations on transaction types.
 * All endpoints are reactive and return Mono or Flux.</p>
 *
 * <p>This controller follows hexagonal architecture by depending on
 * the TransactionTypeUseCase port interface rather than a concrete service.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@RestController
@RequestMapping("/api/transaction-types")
@Tag(name = "Transaction Types", description = "Transaction type management endpoints")
public class TransactionTypeController {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionTypeController.class);

    private final TransactionTypeUseCase transactionTypeUseCase;

    /**
     * Constructs the TransactionTypeController with required dependencies.
     *
     * @param transactionTypeUseCase the transaction type use case port (input port)
     */
    public TransactionTypeController(TransactionTypeUseCase transactionTypeUseCase) {
        this.transactionTypeUseCase = transactionTypeUseCase;
    }

    /**
     * Retrieves all transaction types.
     *
     * @param activeOnly if true, returns only active transaction types
     * @return Flux of TransactionTypeResponse
     */
    @GetMapping
    @Operation(
            summary = "Get all transaction types",
            description = "Retrieves all transaction types ordered by code"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction types retrieved successfully")
    })
    public Flux<TransactionTypeResponse> getAllTransactionTypes(
            @Parameter(description = "Filter to show only active transaction types")
            @RequestParam(defaultValue = "false") boolean activeOnly) {

        LOG.debug("REST request to get all transaction types, activeOnly={}", activeOnly);
        return transactionTypeUseCase.findAll(activeOnly);
    }

    /**
     * Retrieves a transaction type by ID.
     *
     * @param id the transaction type ID
     * @return Mono of TransactionTypeResponse
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get transaction type by ID",
            description = "Retrieves a specific transaction type by its unique identifier"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction type found"),
            @ApiResponse(responseCode = "404", description = "Transaction type not found")
    })
    public Mono<ResponseEntity<TransactionTypeResponse>> getTransactionTypeById(
            @Parameter(description = "Transaction type ID", required = true)
            @PathVariable UUID id) {

        LOG.debug("REST request to get transaction type by id={}", id);
        return transactionTypeUseCase.findById(id)
                .map(ResponseEntity::ok);
    }

    /**
     * Retrieves transaction types by category.
     *
     * @param category the category to filter by
     * @return Flux of TransactionTypeResponse
     */
    @GetMapping("/category/{category}")
    @Operation(
            summary = "Get transaction types by category",
            description = "Retrieves transaction types filtered by category (BANK, CASH, CHECK, OTHER)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction types retrieved successfully")
    })
    public Flux<TransactionTypeResponse> getTransactionTypesByCategory(
            @Parameter(description = "Category (BANK, CASH, CHECK, OTHER)", required = true)
            @PathVariable String category) {

        LOG.debug("REST request to get transaction types by category={}", category);
        return transactionTypeUseCase.findByCategory(category);
    }

    /**
     * Creates a new transaction type.
     *
     * @param request the create request
     * @return Mono of created TransactionTypeResponse
     */
    @PostMapping
    @Operation(
            summary = "Create a new transaction type",
            description = "Creates a new transaction type with the provided information"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transaction type created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Transaction type with this code already exists")
    })
    public Mono<ResponseEntity<TransactionTypeResponse>> createTransactionType(
            @Valid @RequestBody CreateTransactionTypeRequest request) {

        LOG.debug("REST request to create transaction type with code={}", request.getCode());
        return transactionTypeUseCase.create(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    /**
     * Updates an existing transaction type.
     *
     * @param id the transaction type ID to update
     * @param request the update request
     * @return Mono of updated TransactionTypeResponse
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Update a transaction type",
            description = "Updates an existing transaction type with the provided information"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction type updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Transaction type not found"),
            @ApiResponse(responseCode = "409", description = "Transaction type with this code already exists")
    })
    public Mono<ResponseEntity<TransactionTypeResponse>> updateTransactionType(
            @Parameter(description = "Transaction type ID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionTypeRequest request) {

        LOG.debug("REST request to update transaction type id={}", id);
        return transactionTypeUseCase.update(id, request)
                .map(ResponseEntity::ok);
    }

    /**
     * Deletes a transaction type.
     *
     * @param id the transaction type ID to delete
     * @return Mono that completes when deletion is done
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a transaction type",
            description = "Deletes a transaction type by its ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Transaction type deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Transaction type not found")
    })
    public Mono<ResponseEntity<Void>> deleteTransactionType(
            @Parameter(description = "Transaction type ID", required = true)
            @PathVariable UUID id) {

        LOG.debug("REST request to delete transaction type id={}", id);
        return transactionTypeUseCase.delete(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
