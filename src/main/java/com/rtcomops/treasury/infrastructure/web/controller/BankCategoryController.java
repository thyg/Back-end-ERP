package com.rtcomops.treasury.infrastructure.web.controller;

import com.rtcomops.treasury.application.dto.request.BankCategoryRequest;
import com.rtcomops.treasury.application.dto.response.BankCategoryResponse;
import com.rtcomops.treasury.application.port.in.BankCategoryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST Controller for BankCategory operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@RestController
@RequestMapping("/api/bank-categories")
@Tag(name = "Bank Categories", description = "Endpoints for managing bank categories")
public class BankCategoryController {

    private final BankCategoryUseCase bankCategoryUseCase;

    public BankCategoryController(BankCategoryUseCase bankCategoryUseCase) {
        this.bankCategoryUseCase = bankCategoryUseCase;
    }

    @GetMapping
    @Operation(summary = "Get all bank categories")
    public Flux<BankCategoryResponse> getAllBankCategories() {
        return bankCategoryUseCase.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a bank category by ID")
    public Mono<ResponseEntity<BankCategoryResponse>> getBankCategoryById(@PathVariable UUID id) {
        return bankCategoryUseCase.findById(id)
            .map(ResponseEntity::ok);
    }

    @PostMapping
    @Operation(summary = "Create a new bank category")
    public Mono<ResponseEntity<BankCategoryResponse>> createBankCategory(
            @Valid @RequestBody BankCategoryRequest request) {
        return bankCategoryUseCase.create(request)
            .map(category -> ResponseEntity.status(HttpStatus.CREATED).body(category));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a bank category")
    public Mono<ResponseEntity<BankCategoryResponse>> updateBankCategory(
            @PathVariable UUID id,
            @Valid @RequestBody BankCategoryRequest request) {
        return bankCategoryUseCase.update(id, request)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a bank category")
    public Mono<ResponseEntity<Void>> deleteBankCategory(@PathVariable UUID id) {
        return bankCategoryUseCase.delete(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
