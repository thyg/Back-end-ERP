package com.rtcomops.treasury.infrastructure.web.controller;

import com.rtcomops.treasury.application.port.in.BankUseCase;
import com.rtcomops.treasury.infrastructure.web.dto.CreateBankRequest;
import com.rtcomops.treasury.infrastructure.web.dto.UpdateBankRequest;
import com.rtcomops.treasury.infrastructure.web.dto.BankResponse;
import com.rtcomops.treasury.infrastructure.web.mapper.BankApiMapper;
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
 * REST Controller for Bank operations.
 *
 * <p>Provides endpoints for CRUD operations on banks.
 * All endpoints are reactive and return Mono or Flux.</p>
 *
 * <p>This controller follows hexagonal architecture by depending on
 * the BankUseCase port interface rather than a concrete service.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@RestController
@RequestMapping("/api/banks")
@Tag(name = "Banks", description = "Bank management endpoints")
public class BankController {

    private static final Logger LOG = LoggerFactory.getLogger(BankController.class);

    private final BankUseCase bankUseCase;
    private final BankApiMapper mapper;

    /**
     * Constructs the BankController with required dependencies.
     *
     * @param bankUseCase the bank use case port (input port)
     * @param mapper the API mapper for DTO conversions
     */
    public BankController(BankUseCase bankUseCase, BankApiMapper mapper) {
        this.bankUseCase = bankUseCase;
        this.mapper = mapper;
    }

    /**
     * Retrieves all banks.
     *
     * @param activeOnly if true, returns only active banks (default: false)
     * @return Flux of BankResponse
     */
    @GetMapping
    @Operation(
        summary = "Get all banks",
        description = "Retrieves all banks ordered by name. Can filter to show only active banks."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Banks retrieved successfully")
    })
    public Flux<BankResponse> getAllBanks(
            @Parameter(description = "Filter to show only active banks")
            @RequestParam(defaultValue = "false") boolean activeOnly) {

        LOG.debug("REST request to get all banks, activeOnly={}", activeOnly);
        return bankUseCase.findAll(activeOnly)
            .map(mapper::toResponse);
    }

    /**
     * Retrieves a bank by ID.
     *
     * @param id the bank ID
     * @return Mono of BankResponse
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get bank by ID",
        description = "Retrieves a specific bank by its unique identifier"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bank found"),
        @ApiResponse(responseCode = "404", description = "Bank not found")
    })
    public Mono<ResponseEntity<BankResponse>> getBankById(
            @Parameter(description = "Bank ID", required = true)
            @PathVariable UUID id) {

        LOG.debug("REST request to get bank by id={}", id);
        return bankUseCase.findById(id)
            .map(mapper::toResponse)
            .map(ResponseEntity::ok);
    }

    /**
     * Retrieves a bank by code.
     *
     * @param code the bank code
     * @return Mono of BankResponse
     */
    @GetMapping("/code/{code}")
    @Operation(
        summary = "Get bank by code",
        description = "Retrieves a specific bank by its unique code"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bank found"),
        @ApiResponse(responseCode = "404", description = "Bank not found")
    })
    public Mono<ResponseEntity<BankResponse>> getBankByCode(
            @Parameter(description = "Bank code", required = true)
            @PathVariable String code) {

        LOG.debug("REST request to get bank by code={}", code);
        return bankUseCase.findByCode(code)
            .map(mapper::toResponse)
            .map(ResponseEntity::ok);
    }

    /**
     * Creates a new bank.
     *
     * @param request the create request
     * @return Mono of created BankResponse
     */
    @PostMapping
    @Operation(
        summary = "Create a new bank",
        description = "Creates a new bank with the provided information"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Bank created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Bank with this code already exists")
    })
    public Mono<ResponseEntity<BankResponse>> createBank(
            @Valid @RequestBody CreateBankRequest request) {

        LOG.debug("REST request to create bank with code={}", request.getCode());
        return bankUseCase.create(mapper.toDomain(request))
            .map(mapper::toResponse)
            .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    /**
     * Updates an existing bank.
     *
     * @param id the bank ID to update
     * @param request the update request
     * @return Mono of updated BankResponse
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update a bank",
        description = "Updates an existing bank with the provided information"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bank updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Bank not found"),
        @ApiResponse(responseCode = "409", description = "Bank with this code already exists")
    })
    public Mono<ResponseEntity<BankResponse>> updateBank(
            @Parameter(description = "Bank ID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBankRequest request) {

        LOG.debug("REST request to update bank id={}", id);
        return bankUseCase.update(id, mapper.toDomain(request))
            .map(mapper::toResponse)
            .map(ResponseEntity::ok);
    }

    /**
     * Deletes a bank.
     *
     * @param id the bank ID to delete
     * @return Mono that completes when deletion is done
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete a bank",
        description = "Deletes a bank by its ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Bank deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Bank not found")
    })
    public Mono<ResponseEntity<Void>> deleteBank(
            @Parameter(description = "Bank ID", required = true)
            @PathVariable UUID id) {

        LOG.debug("REST request to delete bank id={}", id);
        return bankUseCase.delete(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
