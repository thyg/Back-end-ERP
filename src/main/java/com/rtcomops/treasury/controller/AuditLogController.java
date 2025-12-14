package com.rtcomops.treasury.controller;

import com.rtcomops.treasury.dto.request.AuditLogFilterRequest;
import com.rtcomops.treasury.dto.response.AuditLogResponse;
import com.rtcomops.treasury.enums.AuditAction;
import com.rtcomops.treasury.enums.AuditModule;
import com.rtcomops.treasury.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for AuditLog operations.
 *
 * <p>Provides endpoints for:</p>
 * <ul>
 *   <li>Querying audit logs with various filters</li>
 *   <li>Getting entity history</li>
 *   <li>Retrieving available modules and actions for filtering</li>
 * </ul>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-13
 */
@RestController
@RequestMapping("/api/audit-logs")
@Tag(name = "Audit Logs", description = "Journal des opérations - endpoints de consultation")
public class AuditLogController {

    private static final Logger LOG = LoggerFactory.getLogger(AuditLogController.class);

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    // =========================================================================
    // QUERY ENDPOINTS
    // =========================================================================

    @GetMapping
    @Operation(summary = "Get audit logs with filters", description = "Retrieves paginated audit logs with optional filters")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully")
    })
    public Flux<AuditLogResponse> getAuditLogs(
            @Parameter(description = "Module filter") @RequestParam(required = false) String module,
            @Parameter(description = "Action filter") @RequestParam(required = false) String action,
            @Parameter(description = "Entity ID filter") @RequestParam(required = false) UUID entityId,
            @Parameter(description = "Entity reference filter") @RequestParam(required = false) String entityReference,
            @Parameter(description = "User ID filter") @RequestParam(required = false) UUID userId,
            @Parameter(description = "User name filter") @RequestParam(required = false) String userName,
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Global search term") @RequestParam(required = false) String search,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") Integer size) {

        LOG.debug("REST request to get audit logs with filters");

        AuditLogFilterRequest filter = AuditLogFilterRequest.builder()
            .module(module)
            .action(action)
            .entityId(entityId)
            .entityReference(entityReference)
            .userId(userId)
            .userName(userName)
            .startDate(startDate)
            .endDate(endDate)
            .search(search)
            .page(page)
            .size(size)
            .build();

        return auditLogService.findWithFilters(filter);
    }

    @GetMapping("/count")
    @Operation(summary = "Count audit logs with filters")
    public Mono<ResponseEntity<Map<String, Long>>> countAuditLogs(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String search) {

        AuditLogFilterRequest filter = AuditLogFilterRequest.builder()
            .module(module)
            .action(action)
            .startDate(startDate)
            .endDate(endDate)
            .search(search)
            .build();

        return auditLogService.countWithFilters(filter)
            .map(count -> ResponseEntity.ok(Map.of("total", count)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get audit log by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Audit log found"),
        @ApiResponse(responseCode = "404", description = "Audit log not found")
    })
    public Mono<ResponseEntity<AuditLogResponse>> getAuditLogById(@PathVariable UUID id) {
        LOG.debug("REST request to get audit log by id={}", id);
        return auditLogService.findById(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/entity/{entityId}")
    @Operation(summary = "Get audit history for an entity", description = "Retrieves all audit logs for a specific entity (complete history)")
    public Flux<AuditLogResponse> getEntityHistory(@PathVariable UUID entityId) {
        LOG.debug("REST request to get audit history for entityId={}", entityId);
        return auditLogService.findByEntityId(entityId);
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's audit logs")
    public Flux<AuditLogResponse> getTodayLogs() {
        LOG.debug("REST request to get today's audit logs");
        return auditLogService.findToday();
    }

    // =========================================================================
    // METADATA ENDPOINTS (for filter dropdowns)
    // =========================================================================

    @GetMapping("/modules")
    @Operation(summary = "Get available modules", description = "Returns the list of modules for filter dropdown")
    public Mono<ResponseEntity<List<Map<String, String>>>> getModules() {
        List<Map<String, String>> modules = Arrays.stream(AuditModule.values())
            .map(m -> Map.of(
                "value", m.name(),
                "label", m.getLabel()
            ))
            .collect(Collectors.toList());
        return Mono.just(ResponseEntity.ok(modules));
    }

    @GetMapping("/actions")
    @Operation(summary = "Get available actions", description = "Returns the list of actions for filter dropdown")
    public Mono<ResponseEntity<List<Map<String, String>>>> getActions() {
        List<Map<String, String>> actions = Arrays.stream(AuditAction.values())
            .map(a -> Map.of(
                "value", a.name(),
                "label", a.getLabel(),
                "severity", a.getSeverity()
            ))
            .collect(Collectors.toList());
        return Mono.just(ResponseEntity.ok(actions));
    }
}