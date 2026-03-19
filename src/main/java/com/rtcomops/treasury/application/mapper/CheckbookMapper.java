package com.rtcomops.treasury.application.mapper;

import com.rtcomops.treasury.application.dto.request.CreateCheckbookRequest;
import com.rtcomops.treasury.application.dto.response.CheckbookResponse;
import com.rtcomops.treasury.domain.model.Checkbook;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper for converting between Checkbook domain model and DTOs.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
@Component
public class CheckbookMapper {

    private static final String DEFAULT_STATUS = "ACTIVE";

    /**
     * Converts a CreateCheckbookRequest to a Checkbook domain model.
     *
     * @param request the create request DTO
     * @return a new Checkbook domain model
     */
    public Checkbook toEntity(CreateCheckbookRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Integer endNumber = request.calculateEndNumber();

        return Checkbook.builder()
            .id(UUID.randomUUID())
            .bankAccountId(request.getBankAccountId())
            .iban(request.getIban())
            .prefix(request.getPrefix())
            .startNumber(request.getStartNumber())
            .endNumber(endNumber)
            .numberOfPages(request.getNumberOfPages())
            .currentNumber(request.getStartNumber()) // Start at the beginning
            .status(DEFAULT_STATUS)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    /**
     * Converts a Checkbook domain model to a CheckbookResponse DTO.
     *
     * @param entity the checkbook domain model
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
            .iban(entity.getIban())
            .prefix(entity.getPrefix())
            .startNumber(entity.getStartNumber())
            .endNumber(entity.getEndNumber())
            .numberOfPages(entity.getNumberOfPages())
            .currentNumber(entity.getCurrentNumber())
            .availableChecks(availableChecks)
            .totalChecks(totalChecks)
            .type(entity.getType())
            .isSystem(entity.getIsSystem())
            .status(entity.getStatus())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    /**
     * Converts a Checkbook domain model to a CheckbookResponse DTO with account name.
     *
     * @param entity the checkbook domain model
     * @param accountName the bank account name
     * @return the response DTO with account name
     */
    public CheckbookResponse toResponseWithDetails(Checkbook entity, String accountName) {
        CheckbookResponse response = toResponse(entity);
        response.setBankAccountName(accountName);
        return response;
    }

    /**
     * Creates a copy of the checkbook domain model (for audit purposes).
     *
     * @param original the original domain model
     * @return a copy of the domain model
     */
    public Checkbook copy(Checkbook original) {
        return Checkbook.builder()
            .id(original.getId())
            .bankAccountId(original.getBankAccountId())
            .iban(original.getIban())
            .prefix(original.getPrefix())
            .startNumber(original.getStartNumber())
            .endNumber(original.getEndNumber())
            .numberOfPages(original.getNumberOfPages())
            .currentNumber(original.getCurrentNumber())
            .type(original.getType())
            .isSystem(original.getIsSystem())
            .status(original.getStatus())
            .createdAt(original.getCreatedAt())
            .updatedAt(original.getUpdatedAt())
            .build();
    }
}
