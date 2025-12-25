package com.rtcomops.treasury.mapper;

import com.rtcomops.treasury.dto.request.CreateCheckbookRequest;
import com.rtcomops.treasury.dto.response.CheckbookResponse;
import com.rtcomops.treasury.entity.Checkbook;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper for converting between Checkbook entity and DTOs.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
@Component
public class CheckbookMapper {

    private static final String DEFAULT_STATUS = "ACTIVE";

    /**
     * Converts a CreateCheckbookRequest to a Checkbook entity.
     *
     * @param request the create request DTO
     * @return a new Checkbook entity
     */
    public Checkbook toEntity(CreateCheckbookRequest request) {
        LocalDateTime now = LocalDateTime.now();

        return Checkbook.builder()
            .id(UUID.randomUUID())
            .bankAccountId(request.getBankAccountId())
            .rib(request.getRib())
            .prefix(request.getPrefix())
            .startNumber(request.getStartNumber())
            .endNumber(request.getEndNumber())
            .currentNumber(request.getStartNumber()) // Start at the beginning
            .status(DEFAULT_STATUS)
            .createdAt(now)
            .updatedAt(now)
            .isNew(true)
            .build();
    }

    /**
     * Converts a Checkbook entity to a CheckbookResponse DTO.
     *
     * @param entity the checkbook entity
     * @return the response DTO
     */
    public CheckbookResponse toResponse(Checkbook entity) {
        int availableChecks = 0;
        int totalChecks = 0;

        if (entity.getStartNumber() != null && entity.getEndNumber() != null) {
            totalChecks = entity.getEndNumber() - entity.getStartNumber() + 1;
        }

        if (entity.getCurrentNumber() != null && entity.getEndNumber() != null
            && entity.getCurrentNumber() <= entity.getEndNumber()) {
            availableChecks = entity.getEndNumber() - entity.getCurrentNumber() + 1;
        }

        return CheckbookResponse.builder()
            .id(entity.getId())
            .bankAccountId(entity.getBankAccountId())
            .rib(entity.getRib())
            .prefix(entity.getPrefix())
            .startNumber(entity.getStartNumber())
            .endNumber(entity.getEndNumber())
            .currentNumber(entity.getCurrentNumber())
            .availableChecks(availableChecks)
            .totalChecks(totalChecks)
            .status(entity.getStatus())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    /**
     * Converts a Checkbook entity to a CheckbookResponse DTO with account name.
     *
     * @param entity the checkbook entity
     * @param accountName the bank account name
     * @return the response DTO with account name
     */
    public CheckbookResponse toResponseWithDetails(Checkbook entity, String accountName) {
        CheckbookResponse response = toResponse(entity);
        response.setBankAccountName(accountName);
        return response;
    }

    /**
     * Creates a copy of the checkbook entity (for audit purposes).
     *
     * @param original the original entity
     * @return a copy of the entity
     */
    public Checkbook copy(Checkbook original) {
        return Checkbook.builder()
            .id(original.getId())
            .bankAccountId(original.getBankAccountId())
            .rib(original.getRib())
            .prefix(original.getPrefix())
            .startNumber(original.getStartNumber())
            .endNumber(original.getEndNumber())
            .currentNumber(original.getCurrentNumber())
            .status(original.getStatus())
            .createdAt(original.getCreatedAt())
            .updatedAt(original.getUpdatedAt())
            .isNew(original.isNew())
            .build();
    }
}
