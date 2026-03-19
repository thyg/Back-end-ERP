package com.rtcomops.treasury.infrastructure.web.controller;

import com.rtcomops.treasury.application.dto.response.AccountConnectorResponse;
import com.rtcomops.treasury.domain.model.AccountConnectorType;
import com.rtcomops.treasury.application.port.in.ConfigurationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST Controller for Configuration operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@RestController
@RequestMapping("/api/configuration")
@Tag(name = "Configuration", description = "Endpoints for retrieving configuration models")
public class ConfigurationController {

    private final ConfigurationUseCase configurationUseCase;

    public ConfigurationController(ConfigurationUseCase configurationUseCase) {
        this.configurationUseCase = configurationUseCase;
    }

    @GetMapping("/account-connector-types/{id}")
    @Operation(summary = "Get an account connector type with its dynamic fields")
    public Mono<ResponseEntity<AccountConnectorResponse>> getConnectorType(@PathVariable UUID id) {
        return configurationUseCase.getConnectorTypeWithFields(id)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/account-connector-types")
    @Operation(summary = "Get account connector types filtered by bank category")
    public Flux<AccountConnectorType> getConnectorTypes(
            @RequestParam UUID bankCategoryId) {
        return configurationUseCase.findByBankCategory(bankCategoryId);
    }
}
