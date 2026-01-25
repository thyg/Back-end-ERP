package com.rtcomops.treasury.controller;

import com.rtcomops.treasury.dto.request.BankCategoryRequest;
import com.rtcomops.treasury.entity.BankCategory;
import com.rtcomops.treasury.service.BankCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/bank-categories")
@Tag(name = "Bank Categories", description = "Endpoints for managing bank categories")
public class BankCategoryController {

    private final BankCategoryService bankCategoryService;

    public BankCategoryController(BankCategoryService bankCategoryService) {
        this.bankCategoryService = bankCategoryService;
    }

    @GetMapping
    @Operation(summary = "Get all bank categories")
    public Flux<BankCategory> getAllBankCategories() {
        return bankCategoryService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a bank category by ID")
    public Mono<ResponseEntity<BankCategory>> getBankCategoryById(@PathVariable UUID id) {
        return bankCategoryService.findById(id)
                .map(ResponseEntity::ok);
    }

    @PostMapping
    @Operation(summary = "Create a new bank category")
    public Mono<ResponseEntity<BankCategory>> createBankCategory(
            @Valid @RequestBody BankCategoryRequest request) {
        return bankCategoryService.create(request.getCode(), request.getLabel())
                .map(category -> ResponseEntity.status(HttpStatus.CREATED).body(category));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a bank category")
    public Mono<ResponseEntity<BankCategory>> updateBankCategory(
            @PathVariable UUID id,
            @Valid @RequestBody BankCategoryRequest request) {
        return bankCategoryService.update(id, request.getCode(), request.getLabel())
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a bank category")
    public Mono<ResponseEntity<Void>> deleteBankCategory(@PathVariable UUID id) {
        return bankCategoryService.delete(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
