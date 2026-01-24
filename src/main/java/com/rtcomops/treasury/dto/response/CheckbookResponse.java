package com.rtcomops.treasury.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for checkbook response data.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckbookResponse {

    /**
     * Unique identifier of the checkbook.
     */
    private UUID id;

    /**
     * Bank account ID.
     */
    private UUID bankAccountId;

    /**
     * Bank account name/label for display.
     */
    private String bankAccountName;

    /**
     * IBAN associated with the checkbook.
     */
    private String iban;

    /**
     * Common prefix/root for check numbers.
     */
    private String prefix;

    /**
     * First check number in the range.
     */
    private Integer startNumber;

    /**
     * Last check number in the range.
     */
    private Integer endNumber;

    /**
     * Next available check number.
     */
    private Integer currentNumber;

    /**
     * Number of checks still available (endNumber - currentNumber + 1).
     */
    private Integer availableChecks;

    /**
     * Number of pages (checks) in the checkbook.
     */
    private Integer numberOfPages;

    /**
     * Total number of checks in the checkbook.
     */
    private Integer totalChecks;

    /**
     * Checkbook type: REEL or FICTIF.
     */
    private String type;

    /**
     * Whether this is a system-managed checkbook.
     */
    private Boolean isSystem;

    /**
     * Status: ACTIVE, FINISHED, or CANCELLED.
     */
    private String status;

    /**
     * Creation timestamp.
     */
    private LocalDateTime createdAt;

    /**
     * Last update timestamp.
     */
    private LocalDateTime updatedAt;
}
