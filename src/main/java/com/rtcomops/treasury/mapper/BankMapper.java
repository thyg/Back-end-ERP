package com.rtcomops.treasury.mapper;

import com.rtcomops.treasury.dto.request.CreateBankRequest;
import com.rtcomops.treasury.dto.request.UpdateBankRequest;
import com.rtcomops.treasury.dto.response.BankResponse;
import com.rtcomops.treasury.entity.Bank;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper for converting between Bank entity and DTOs.
 *
 * <p>Provides methods for transforming request DTOs to entities
 * and entities to response DTOs.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@Component
public class BankMapper {

    /**
     * Converts a CreateBankRequest to a Bank entity.
     * The entity is marked as new for proper INSERT behavior.
     *
     * @param request the create request DTO
     * @return a new Bank entity ready for persistence
     */
    public Bank toEntity(CreateBankRequest request) {
        LocalDateTime now = LocalDateTime.now();
        
        return Bank.builder()
            .id(UUID.randomUUID())
            .code(request.getCode().toUpperCase())
            .name(request.getName())
            .swiftCode(request.getSwiftCode())
            .country(request.getCountry())
            .isActive(request.getIsActive() != null ? request.getIsActive() : true)
            .createdAt(now)
            .updatedAt(now)
            .isNew(true)  // Mark as new for INSERT
            .build();
    }

    /**
     * Updates an existing Bank entity with values from UpdateBankRequest.
     * The entity is marked as NOT new for proper UPDATE behavior.
     *
     * @param existing the existing bank entity
     * @param request the update request DTO
     * @return the updated Bank entity
     */
    public Bank updateEntity(Bank existing, UpdateBankRequest request) {
        if (request.getCode() != null) {
            existing.setCode(request.getCode().toUpperCase());
        }
        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getSwiftCode() != null) {
            existing.setSwiftCode(request.getSwiftCode());
        }
        if (request.getCountry() != null) {
            existing.setCountry(request.getCountry());
        }
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setNew(false);  // Mark as NOT new for UPDATE
        
        return existing;
    }

    /**
     * Converts a Bank entity to a BankResponse DTO.
     *
     * @param entity the bank entity
     * @return the response DTO
     */
    public BankResponse toResponse(Bank entity) {
        return BankResponse.builder()
            .id(entity.getId())
            .code(entity.getCode())
            .name(entity.getName())
            .swiftCode(entity.getSwiftCode())
            .country(entity.getCountry())
            .isActive(entity.getIsActive())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
