package com.rtcomops.treasury.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for filtering audit log queries.
 *
 * <p>All fields are optional. When multiple fields are provided,
 * they are combined with AND logic.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogFilterRequest {

    /**
     * Filter by module (BANK, BANK_ACCOUNT, CHECK, etc.).
     */
    private String module;

    /**
     * Filter by action (CREATE, UPDATE, DELETE, etc.).
     */
    private String action;

    /**
     * Filter by specific entity ID.
     */
    private UUID entityId;

    /**
     * Filter by entity reference (partial match).
     */
    private String entityReference;

    /**
     * Filter by user ID.
     */
    private UUID userId;

    /**
     * Filter by user name (partial match).
     */
    private String userName;

    /**
     * Start date for date range filter.
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    /**
     * End date for date range filter.
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    /**
     * Global search term (searches description and reference).
     */
    private String search;

    /**
     * Page number (0-indexed).
     */
    @Builder.Default
    private Integer page = 0;

    /**
     * Page size.
     */
    @Builder.Default
    private Integer size = 50;
}