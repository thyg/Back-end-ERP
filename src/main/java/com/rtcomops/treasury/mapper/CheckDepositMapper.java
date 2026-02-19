package com.rtcomops.treasury.mapper;

import com.rtcomops.treasury.dto.response.CheckDepositResponse;
import com.rtcomops.treasury.dto.response.CheckResponse;
import com.rtcomops.treasury.entity.CheckDeposit;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting between CheckDeposit entity and DTOs.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2026-02-16
 */
@Component
public class CheckDepositMapper {

    /**
     * Converts a CheckDeposit entity to a CheckDepositResponse.
     *
     * @param entity the check deposit entity
     * @return the response DTO
     */
    public CheckDepositResponse toResponse(CheckDeposit entity) {
        return CheckDepositResponse.builder()
            .id(entity.getId())
            .reference(entity.getReference())
            .depositDate(entity.getDepositDate())
            .bankAccountId(entity.getBankAccountId())
            .totalAmount(entity.getTotalAmount())
            .checkCount(entity.getCheckCount())
            .status(entity.getStatus())
            .cashedDate(entity.getCashedDate())
            .bankTransactionId(entity.getBankTransactionId())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    /**
     * Converts a CheckDeposit entity to a CheckDepositResponse with bank account details.
     *
     * @param entity the check deposit entity
     * @param bankAccountName the bank account name
     * @param currency the bank account currency
     * @return the response DTO with details
     */
    public CheckDepositResponse toResponseWithAccountDetails(
            CheckDeposit entity, String bankAccountName, String currency) {
        CheckDepositResponse response = toResponse(entity);
        response.setBankAccountName(bankAccountName);
        response.setCurrency(currency);
        return response;
    }

    /**
     * Converts a CheckDeposit entity to a CheckDepositResponse with full details.
     *
     * @param entity the check deposit entity
     * @param bankAccountName the bank account name
     * @param currency the bank account currency
     * @param bankTransactionReference the bank transaction reference (if reconciled)
     * @param checks the list of checks in this deposit
     * @return the response DTO with all details
     */
    public CheckDepositResponse toResponseWithFullDetails(
            CheckDeposit entity,
            String bankAccountName,
            String currency,
            String bankTransactionReference,
            List<CheckResponse> checks) {
        CheckDepositResponse response = toResponseWithAccountDetails(entity, bankAccountName, currency);
        response.setBankTransactionReference(bankTransactionReference);
        response.setChecks(checks);
        return response;
    }
}
