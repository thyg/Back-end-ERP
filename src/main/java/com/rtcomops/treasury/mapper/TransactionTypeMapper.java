package com.rtcomops.treasury.mapper;

import com.rtcomops.treasury.dto.request.CreateTransactionTypeRequest;
import com.rtcomops.treasury.dto.request.UpdateTransactionTypeRequest;
import com.rtcomops.treasury.dto.response.TransactionTypeResponse;
import com.rtcomops.treasury.entity.TransactionType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper for converting between TransactionType entity and DTOs.
 *
 * <p>Provides methods for transforming request DTOs to entities
 * and entities to response DTOs.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@Component
public class TransactionTypeMapper {

    /**
     * Converts a CreateTransactionTypeRequest to a TransactionType entity.
     * The entity is marked as new for proper INSERT behavior.
     *
     * @param request the create request DTO
     * @return a new TransactionType entity ready for persistence
     */
    public TransactionType toEntity(CreateTransactionTypeRequest request) {
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
            .isNew(true)  // Mark as new for INSERT
            .build();
    }

    /**
     * Updates an existing TransactionType entity with values from UpdateTransactionTypeRequest.
     * The entity is marked as NOT new for proper UPDATE behavior.
     *
     * @param existing the existing entity
     * @param request the update request DTO
     * @return the updated TransactionType entity
     */
    public TransactionType updateEntity(TransactionType existing, UpdateTransactionTypeRequest request) {
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
        existing.setNew(false);  // Mark as NOT new for UPDATE
        
        return existing;
    }

    /**
     * Converts a TransactionType entity to a TransactionTypeResponse DTO.
     *
     * @param entity the transaction type entity
     * @return the response DTO
     */
    public TransactionTypeResponse toResponse(TransactionType entity) {
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
