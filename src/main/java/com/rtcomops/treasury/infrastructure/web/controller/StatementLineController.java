package com.rtcomops.treasury.infrastructure.adapter.incoming.rest;

import com.rtcomops.treasury.application.dto.request.CreateStatementLineRequest;
import com.rtcomops.treasury.application.dto.response.StatementLineResponse;
import com.rtcomops.treasury.application.port.in.StatementLineUseCase;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for StatementLine operations.
 *
 * <p>This controller is an adapter in the hexagonal architecture,
 * adapting HTTP requests to the domain's StatementLineUseCase port.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@RestController
@RequestMapping("/api/statement-lines")
@Tag(name = "Statement Lines", description = "Statement line management endpoints")
public class StatementLineController {

    private static final Logger LOG = LoggerFactory.getLogger(StatementLineController.class);

    private final StatementLineUseCase statementLineUseCase;

    public StatementLineController(StatementLineUseCase statementLineUseCase) {
        this.statementLineUseCase = statementLineUseCase;
    }

    @GetMapping("/statement/{statementId}")
    @Operation(summary = "Get all lines for a statement")
    public Flux<StatementLineResponse> getLinesByStatement(@PathVariable UUID statementId) {
        LOG.debug("REST request to get lines for statement id={}", statementId);
        return statementLineUseCase.findByStatementId(statementId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get statement line by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Line found"),
        @ApiResponse(responseCode = "404", description = "Line not found")
    })
    public Mono<ResponseEntity<StatementLineResponse>> getLineById(@PathVariable UUID id) {
        LOG.debug("REST request to get statement line by id={}", id);
        return statementLineUseCase.findById(id).map(ResponseEntity::ok);
    }

    @GetMapping("/statement/{statementId}/unmatched")
    @Operation(summary = "Get unmatched lines for a statement")
    public Flux<StatementLineResponse> getUnmatchedLines(@PathVariable UUID statementId) {
        LOG.debug("REST request to get unmatched lines for statement id={}", statementId);
        return statementLineUseCase.findUnmatchedByStatementId(statementId);
    }

    @GetMapping("/statement/{statementId}/matched")
    @Operation(summary = "Get matched lines for a statement")
    public Flux<StatementLineResponse> getMatchedLines(@PathVariable UUID statementId) {
        LOG.debug("REST request to get matched lines for statement id={}", statementId);
        return statementLineUseCase.findMatchedByStatementId(statementId);
    }

    @PostMapping
    @Operation(summary = "Create a new statement line")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Line created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Statement not found")
    })
    public Mono<ResponseEntity<StatementLineResponse>> createLine(
            @Valid @RequestBody CreateStatementLineRequest request) {
        LOG.debug("REST request to create statement line");
        return statementLineUseCase.create(request)
            .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @PostMapping("/statement/{statementId}/batch")
    @Operation(summary = "Create multiple statement lines")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Lines created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Statement not found")
    })
    public Flux<StatementLineResponse> createLinesBatch(
            @PathVariable UUID statementId,
            @Valid @RequestBody List<CreateStatementLineRequest> requests) {
        LOG.debug("REST request to create {} lines for statement id={}", requests.size(), statementId);
        return statementLineUseCase.createBatch(statementId, requests);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a statement line")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Line deleted"),
        @ApiResponse(responseCode = "404", description = "Line not found")
    })
    public Mono<ResponseEntity<Void>> deleteLine(@PathVariable UUID id) {
        LOG.debug("REST request to delete statement line id={}", id);
        return statementLineUseCase.delete(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @PostMapping("/{id}/ignore")
    @Operation(summary = "Mark line as ignored")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Line marked as ignored"),
        @ApiResponse(responseCode = "404", description = "Line not found")
    })
    public Mono<ResponseEntity<StatementLineResponse>> ignoreLine(@PathVariable UUID id) {
        LOG.debug("REST request to ignore statement line id={}", id);
        return statementLineUseCase.ignore(id).map(ResponseEntity::ok);
    }

    @PostMapping("/{id}/reset")
    @Operation(summary = "Reset line to unmatched")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Line reset to unmatched"),
        @ApiResponse(responseCode = "404", description = "Line not found")
    })
    public Mono<ResponseEntity<StatementLineResponse>> resetLine(@PathVariable UUID id) {
        LOG.debug("REST request to reset statement line id={}", id);
        return statementLineUseCase.resetToUnmatched(id).map(ResponseEntity::ok);
    }
}
