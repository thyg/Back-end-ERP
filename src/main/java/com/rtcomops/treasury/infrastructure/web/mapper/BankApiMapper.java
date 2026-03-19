package com.rtcomops.treasury.infrastructure.web.mapper;

import com.rtcomops.treasury.domain.model.Bank;
import com.rtcomops.treasury.infrastructure.web.dto.CreateBankRequest;
import com.rtcomops.treasury.infrastructure.web.dto.UpdateBankRequest;
import com.rtcomops.treasury.infrastructure.web.dto.BankResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Bank domain model and web DTOs.
 *
 * <p>This mapper is used by the web controller to convert between
 * request/response DTOs and the domain model.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@Component
public class BankApiMapper {

    /**
     * Converts a CreateBankRequest DTO to a Bank domain model.
     * Only maps the fields from the request; the service layer will
     * set ID, timestamps, and defaults.
     *
     * @param request the create request DTO
     * @return a Bank domain model
     */
    public Bank toDomain(CreateBankRequest request) {
        if (request == null) {
            return null;
        }

        return Bank.builder()
            .code(request.getCode())
            .name(request.getName())
            .swiftCode(request.getSwiftCode())
            .bankCode(request.getBankCode())
            .country(request.getCountry())
            .address(request.getAddress())
            .isActive(request.getIsActive())
            .bankCategoryId(request.getBankCategoryId())
            .build();
    }

    /**
     * Converts an UpdateBankRequest DTO to a Bank domain model.
     * Only maps the fields from the request for partial updates.
     *
     * @param request the update request DTO
     * @return a Bank domain model with update values
     */
    public Bank toDomain(UpdateBankRequest request) {
        if (request == null) {
            return null;
        }

        return Bank.builder()
            .code(request.getCode())
            .name(request.getName())
            .swiftCode(request.getSwiftCode())
            .bankCode(request.getBankCode())
            .country(request.getCountry())
            .address(request.getAddress())
            .isActive(request.getIsActive())
            .bankCategoryId(request.getBankCategoryId())
            .build();
    }

    /**
     * Converts a Bank domain model to a BankResponse DTO.
     *
     * @param domain the Bank domain model
     * @return the response DTO
     */
    public BankResponse toResponse(Bank domain) {
        if (domain == null) {
            return null;
        }

        return BankResponse.builder()
            .id(domain.getId())
            .code(domain.getCode())
            .name(domain.getName())
            .swiftCode(domain.getSwiftCode())
            .bankCode(domain.getBankCode())
            .country(domain.getCountry())
            .address(domain.getAddress())
            .isActive(domain.getIsActive())
            .bankCategoryId(domain.getBankCategoryId())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .build();
    }
}
