package com.rtcomops.treasury.mapper;

import com.rtcomops.treasury.dto.request.CreateBankAccountRequest;
import com.rtcomops.treasury.dto.request.UpdateBankAccountRequest;
import com.rtcomops.treasury.dto.response.BankAccountResponse;
import com.rtcomops.treasury.entity.BankAccount;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper for converting between BankAccount entity and DTOs.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Component
public class BankAccountMapper {

    public BankAccount toEntity(CreateBankAccountRequest request) {
        LocalDateTime now = LocalDateTime.now();
        BigDecimal initialBalance = request.getInitialBalance() != null 
            ? request.getInitialBalance() 
            : BigDecimal.ZERO;
        
        return BankAccount.builder()
            .id(UUID.randomUUID())
            .bankId(request.getBankId())
            .name(request.getName())
            .accountNumber(request.getAccountNumber())
            .iban(request.getIban())
            .bic(request.getBic())
            .currency(request.getCurrency() != null ? request.getCurrency() : "EUR")
            .currentBalance(initialBalance)
            .reconciledBalance(BigDecimal.ZERO)
            .isActive(request.getIsActive() != null ? request.getIsActive() : true)
            .createdAt(now)
            .updatedAt(now)
            .isNew(true)
            .build();
    }

    public BankAccount updateEntity(BankAccount existing, UpdateBankAccountRequest request) {
        if (request.getBankId() != null) {
            existing.setBankId(request.getBankId());
        }
        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getAccountNumber() != null) {
            existing.setAccountNumber(request.getAccountNumber());
        }
        if (request.getIban() != null) {
            existing.setIban(request.getIban());
        }
        if (request.getBic() != null) {
            existing.setBic(request.getBic());
        }
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setNew(false);
        
        return existing;
    }

    public BankAccountResponse toResponse(BankAccount entity) {
        return BankAccountResponse.builder()
            .id(entity.getId())
            .bankId(entity.getBankId())
            .name(entity.getName())
            .accountNumber(entity.getAccountNumber())
            .iban(entity.getIban())
            .bic(entity.getBic())
            .currency(entity.getCurrency())
            .currentBalance(entity.getCurrentBalance())
            .reconciledBalance(entity.getReconciledBalance())
            .isActive(entity.getIsActive())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    public BankAccountResponse toResponseWithBankName(BankAccount entity, String bankName) {
        BankAccountResponse response = toResponse(entity);
        response.setBankName(bankName);
        return response;
    }
}
