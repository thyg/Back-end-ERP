package com.rtcomops.treasury.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller for Treasury API (Reactive WebFlux).
 *
 * <p>Provides reactive endpoints to verify the API is running correctly.
 * All methods return Mono or Flux for non-blocking operations.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Health check endpoints (Reactive)")
public class HealthController {

    /**
     * Reactive health check endpoint.
     *
     * @return Mono containing health status with timestamp
     */
    @GetMapping
    @Operation(
        summary = "Health check (Reactive)",
        description = "Returns the health status of the Treasury API as a reactive Mono"
    )
    public Mono<Map<String, Object>> healthCheck() {
        return Mono.fromSupplier(() -> {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "UP");
            response.put("service", "Treasury API");
            response.put("version", "1.0.0");
            response.put("architecture", "Hexagonal");
            response.put("reactive", true);
            response.put("framework", "Spring WebFlux");
            response.put("timestamp", LocalDateTime.now().toString());
            return response;
        });
    }

    /**
     * Reactive ping endpoint for simple connectivity test.
     *
     * @return Mono containing pong response
     */
    @GetMapping("/ping")
    @Operation(
        summary = "Ping (Reactive)",
        description = "Simple reactive ping endpoint to test connectivity"
    )
    public Mono<String> ping() {
        return Mono.just("pong");
    }

    /**
     * Endpoint to demonstrate reactive nature with simulated delay.
     *
     * @return Mono containing response after non-blocking delay
     */
    @GetMapping("/reactive-test")
    @Operation(
        summary = "Reactive Test",
        description = "Demonstrates non-blocking reactive behavior"
    )
    public Mono<Map<String, Object>> reactiveTest() {
        return Mono.fromSupplier(() -> {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "This response is non-blocking!");
            response.put("thread", Thread.currentThread().getName());
            response.put("timestamp", LocalDateTime.now().toString());
            return response;
        });
    }
}
