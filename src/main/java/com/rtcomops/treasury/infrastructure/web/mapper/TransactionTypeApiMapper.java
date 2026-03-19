package com.rtcomops.treasury.infrastructure.web.mapper;

import com.rtcomops.treasury.application.dto.request.CreateTransactionTypeRequest;
import com.rtcomops.treasury.application.dto.request.UpdateTransactionTypeRequest;
import com.rtcomops.treasury.application.dto.response.TransactionTypeResponse;
import com.rtcomops.treasury.domain.model.TransactionType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper for converting between TransactionType domain model and web DTOs.
 *
 * <p>This mapper is used by the web controller to convert between
 * request/response DTOs and the domain model.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@Component
public class TransactionTypeApiMapper {

    /**
     * Converts a CreateTransactionTypeRequest DTO to a TransactionType domain model.
     * Sets ID, timestamps, and defaults.
     *
     * @param request the create request DTO
     * @return a TransactionType domain model ready for persistence
     */
    public TransactionType toEntity(CreateTransactionTypeRequest request) {
        if (request == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();

        return TransactionType.builder()
                .id(UUID.randomUUID())
                .code(request.getCode().toUpperCase())
                .label(request.getLabel())
                .category(request.getCategory().toUpperCase())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Updates an existing TransactionType domain model with values from UpdateTransactionTypeRequest.
     *
     * @param existing the existing domain model
     * @param request the update request DTO
     * @return the updated TransactionType domain model
     */
    public TransactionType updateEntity(TransactionType existing, UpdateTransactionTypeRequest request) {
        if (request == null) {
            return existing;
        }

        if (request.getCode() != null) {
            existing.setCode(request.getCode().toUpperCase());
        }
        if (request.getLabel() != null) {
            existing.setLabel(request.getLabel());
        }
        if (request.getCategory() != null) {
            existing.setCategory(request.getCategory().toUpperCase());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        return existing;
    }

    /**
     * Converts a TransactionType domain model to a TransactionTypeResponse DTO.
     *
     * @param entity the TransactionType domain model
     * @return the response DTO
     */
    public TransactionTypeResponse toResponse(TransactionType entity) {
        if (entity == null) {
            return null;
        }

        return TransactionTypeResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .label(entity.getLabel())
                .category(entity.getCategory())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
