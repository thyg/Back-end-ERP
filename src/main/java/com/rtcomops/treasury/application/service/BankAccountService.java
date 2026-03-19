package com.rtcomops.treasury.application.service;

import com.rtcomops.treasury.application.dto.request.CreateBankAccountRequest;
import com.rtcomops.treasury.application.dto.request.UpdateBankAccountRequest;
import com.rtcomops.treasury.application.dto.response.BankAccountResponse;
import com.rtcomops.treasury.domain.exception.ResourceNotFoundException;
import com.rtcomops.treasury.domain.model.Bank;
import com.rtcomops.treasury.domain.model.BankAccount;
import com.rtcomops.treasury.domain.model.enums.AuditAction;
import com.rtcomops.treasury.domain.model.enums.AuditModule;
import com.rtcomops.treasury.application.port.in.AuditLogUseCase;
import com.rtcomops.treasury.application.port.in.BankAccountUseCase;
import com.rtcomops.treasury.domain.port.out.BankRepositoryPort;
import com.rtcomops.treasury.domain.port.out.BankAccountRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Domain service for BankAccount operations.
 *
 * <p>Implements the BankAccountUseCase port and provides business logic
 * for managing bank accounts, including CRUD operations with validation
 * and error handling.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Service
@Transactional
public class BankAccountService implements BankAccountUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(BankAccountService.class);
    private static final String RESOURCE_NAME = "BankAccount";

    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final BankRepositoryPort bankRepositoryPort;
    private final AuditLogUseCase auditLogUseCase;

    /**
     * Constructs the BankAccountService with required dependencies.
     *
     * @param bankAccountRepositoryPort the bank account repository port
     * @param bankRepositoryPort the bank repository port (for enrichWithBankName)
     * @param auditLogUseCase the audit log use case
     */
    public BankAccountService(
            BankAccountRepositoryPort bankAccountRepositoryPort,
            BankRepositoryPort bankRepositoryPort,
            AuditLogUseCase auditLogUseCase) {
        this.bankAccountRepositoryPort = bankAccountRepositoryPort;
        this.bankRepositoryPort = bankRepositoryPort;
        this.auditLogUseCase = auditLogUseCase;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Flux<BankAccountResponse> findAll(boolean activeOnly) {
        LOG.debug("Finding all bank accounts, activeOnly={}", activeOnly);

        Flux<BankAccount> accounts = activeOnly
                ? bankAccountRepositoryPort.findAllActiveOrderByName()
                : bankAccountRepositoryPort.findAllOrderByName();

        return accounts.flatMap(this::enrichWithBankName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Mono<BankAccountResponse> findById(UUID id) {
        LOG.debug("Finding bank account by id={}", id);

        return bankAccountRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
                .flatMap(this::enrichWithBankName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Flux<BankAccountResponse> findByBankId(UUID bankId) {
        LOG.debug("Finding bank accounts by bankId={}", bankId);

        return bankAccountRepositoryPort.findByBankId(bankId)
                .flatMap(this::enrichWithBankName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<BankAccountResponse> create(CreateBankAccountRequest request) {
        LOG.info("Creating bank account with name={}", request.getName());

        return bankRepositoryPort.findById(request.getBankId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Bank", request.getBankId())))
                .flatMap(bank -> {
                    BankAccount entity = toEntity(request);
                    return bankAccountRepositoryPort.save(entity)
                            .doOnSuccess(saved -> LOG.info("Bank account created: id={}, name={}",
                                    saved.getId(), saved.getName()))
                            .flatMap(saved -> auditLogUseCase.log(
                                    AuditModule.BANK_ACCOUNT,
                                    AuditAction.CREATE,
                                    saved.getId(),
                                    saved.getName(),
                                    null,
                                    saved,
                                    "Création du compte bancaire: " + saved.getName()
                            ).thenReturn(toResponseWithBankName(saved, bank.getName())));
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<BankAccountResponse> update(UUID id, UpdateBankAccountRequest request) {
        LOG.info("Updating bank account id={}", id);

        return bankAccountRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
                .flatMap(existing -> {
                    // Copier l'état précédent pour l'audit
                    BankAccount oldState = copyBankAccount(existing);

                    BankAccount updated = updateEntity(existing, request);
                    return bankAccountRepositoryPort.save(updated)
                            .doOnSuccess(saved -> LOG.info("Bank account updated: id={}", saved.getId()))
                            .flatMap(saved -> auditLogUseCase.log(
                                    AuditModule.BANK_ACCOUNT,
                                    AuditAction.UPDATE,
                                    saved.getId(),
                                    saved.getName(),
                                    oldState,
                                    saved,
                                    "Mise à jour du compte bancaire: " + saved.getName()
                            ).then(enrichWithBankName(saved)));
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> delete(UUID id) {
        LOG.info("Deleting bank account id={}", id);

        return bankAccountRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
                .flatMap(account -> auditLogUseCase.log(
                        AuditModule.BANK_ACCOUNT,
                        AuditAction.DELETE,
                        account.getId(),
                        account.getName(),
                        account,
                        null,
                        "Suppression du compte bancaire: " + account.getName()
                    ).then(bankAccountRepositoryPort.delete(account))
                        .doOnSuccess(v -> LOG.info("Bank account deleted: id={}, name={}",
                                account.getId(), account.getName())));
    }

    // ========== Helper Methods ==========

    /**
     * Enriches a BankAccount with its bank name.
     */
    private Mono<BankAccountResponse> enrichWithBankName(BankAccount account) {
        return bankRepositoryPort.findById(account.getBankId())
                .map(bank -> toResponseWithBankName(account, bank.getName()))
                .defaultIfEmpty(toResponse(account));
    }

    // ========== Mapping Methods ==========

    /**
     * Converts a CreateBankAccountRequest to a BankAccount domain model.
     */
    private BankAccount toEntity(CreateBankAccountRequest request) {
        LocalDateTime now = LocalDateTime.now();

        return BankAccount.builder()
                .id(UUID.randomUUID())
                .bankId(request.getBankId())
                .accountTypeId(request.getAccountTypeId())
                .accountSubTypeId(request.getAccountSubTypeId())
                .connectorTypeId(request.getConnectorTypeId())
                .name(request.getName())
                .branchCode(request.getBranchCode())
                .generatedIban(request.getGeneratedIban())
                .currency(request.getCurrency() != null ? request.getCurrency() : "XAF")
                .currentBalance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO)
                .reconciledBalance(BigDecimal.ZERO)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .details(request.getDetails())
                .overdraftAuthorized(request.getOverdraftAllowed() != null ? request.getOverdraftAllowed() : false)
                .overdraftLimit(request.getOverdraftLimit())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Updates an existing BankAccount domain model with values from UpdateBankAccountRequest.
     */
    private BankAccount updateEntity(BankAccount existing, UpdateBankAccountRequest request) {
        if (request.getBankId() != null) {
            existing.setBankId(request.getBankId());
        }
        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        return existing;
    }

    /**
     * Converts a BankAccount domain model to a BankAccountResponse DTO.
     */
    private BankAccountResponse toResponse(BankAccount entity) {
        Map<String, Object> detailsMap = entity.getDetails();

        return BankAccountResponse.builder()
                .id(entity.getId())
                .bankId(entity.getBankId())
                .name(entity.getName())
                .branchCode(entity.getBranchCode())
                .accountNumber(detailsMap != null ? (String) detailsMap.get("accountNumber") : null)
                .generatedIban(entity.getGeneratedIban())
                .iban(detailsMap != null ? (String) detailsMap.get("iban") : null)
                .bic(detailsMap != null ? (String) detailsMap.get("bic") : null)
                .details(detailsMap)
                .currency(entity.getCurrency())
                .currentBalance(entity.getCurrentBalance())
                .reconciledBalance(entity.getReconciledBalance())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Converts a BankAccount domain model to a BankAccountResponse DTO with bank name.
     */
    private BankAccountResponse toResponseWithBankName(BankAccount entity, String bankName) {
        BankAccountResponse response = toResponse(entity);
        response.setBankName(bankName);
        return response;
    }

    /**
     * Creates a copy of a BankAccount for audit purposes.
     */
    private BankAccount copyBankAccount(BankAccount original) {
        return BankAccount.builder()
                .id(original.getId())
                .bankId(original.getBankId())
                .accountTypeId(original.getAccountTypeId())
                .accountSubTypeId(original.getAccountSubTypeId())
                .connectorTypeId(original.getConnectorTypeId())
                .name(original.getName())
                .branchCode(original.getBranchCode())
                .generatedIban(original.getGeneratedIban())
                .currency(original.getCurrency())
                .currentBalance(original.getCurrentBalance())
                .reconciledBalance(original.getReconciledBalance())
                .isActive(original.getIsActive())
                .details(original.getDetails())
                .overdraftAuthorized(original.getOverdraftAuthorized())
                .overdraftLimit(original.getOverdraftLimit())
                .createdAt(original.getCreatedAt())
                .updatedAt(original.getUpdatedAt())
                .build();
    }
}
