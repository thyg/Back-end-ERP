package com.rtcomops.treasury.application.mapper;

import com.rtcomops.treasury.application.dto.response.CheckDepositResponse;
import com.rtcomops.treasury.application.dto.response.CheckResponse;
import com.rtcomops.treasury.domain.model.CheckDeposit;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting between CheckDeposit domain model and DTOs.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-13
 */
@Component
public class CheckDepositMapper {

    /**
     * Converts a CheckDeposit domain model to a CheckDepositResponse.
     *
     * @param domain the check deposit domain model
     * @return the response DTO
     */
    public CheckDepositResponse toResponse(CheckDeposit domain) {
        return CheckDepositResponse.builder()
            .id(domain.getId())
            .reference(domain.getReference())
            .depositDate(domain.getDepositDate())
            .bankAccountId(domain.getBankAccountId())
            .totalAmount(domain.getTotalAmount())
            .checkCount(domain.getCheckCount())
            .status(domain.getStatus())
            .cashedDate(domain.getCashedDate())
            .bankTransactionId(domain.getBankTransactionId())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .build();
    }

    /**
     * Converts a CheckDeposit domain model to a CheckDepositResponse with bank account details.
     *
     * @param domain the check deposit domain model
     * @param bankAccountName the bank account name
     * @param currency the bank account currency
     * @return the response DTO with details
     */
    public CheckDepositResponse toResponseWithAccountDetails(
            CheckDeposit domain, String bankAccountName, String currency) {
        CheckDepositResponse response = toResponse(domain);
        response.setBankAccountName(bankAccountName);
        response.setCurrency(currency);
        return response;
    }

    /**
     * Converts a CheckDeposit domain model to a CheckDepositResponse with full details.
     *
     * @param domain the check deposit domain model
     * @param bankAccountName the bank account name
     * @param currency the bank account currency
     * @param bankTransactionReference the bank transaction reference (if reconciled)
     * @param checks the list of checks in this deposit
     * @return the response DTO with all details
     */
    public CheckDepositResponse toResponseWithFullDetails(
            CheckDeposit domain,
            String bankAccountName,
            String currency,
            String bankTransactionReference,
            List<CheckResponse> checks) {
        CheckDepositResponse response = toResponseWithAccountDetails(domain, bankAccountName, currency);
        response.setBankTransactionReference(bankTransactionReference);
        response.setChecks(checks);
        return response;
    }
}
