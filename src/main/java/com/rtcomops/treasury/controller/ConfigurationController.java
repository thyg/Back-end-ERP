package com.rtcomops.treasury.controller;

import com.rtcomops.treasury.dto.response.AccountConnectorResponse;
import com.rtcomops.treasury.entity.AccountConnectorType;
import com.rtcomops.treasury.service.ConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/configuration")
@Tag(name = "Configuration", description = "Endpoints for retrieving configuration models")
public class ConfigurationController {

    private final ConfigurationService configurationService;

    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @GetMapping("/account-connector-types/{id}")
    @Operation(summary = "Get an account connector type with its dynamic fields")
    public Mono<ResponseEntity<AccountConnectorResponse>> getConnectorType(@PathVariable UUID id) {
        return configurationService.getConnectorTypeWithFields(id)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/account-connector-types")
    @Operation(summary = "Get account connector types filtered by bank category")
    public Flux<AccountConnectorType> getConnectorTypes(
            @RequestParam UUID bankCategoryId) {
        return configurationService.findByBankCategory(bankCategoryId);
    }
}
