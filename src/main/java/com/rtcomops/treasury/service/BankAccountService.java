package com.rtcomops.treasury.service;

import com.rtcomops.treasury.dto.request.CreateBankAccountRequest;
import com.rtcomops.treasury.dto.request.UpdateBankAccountRequest;
import com.rtcomops.treasury.dto.response.BankAccountResponse;
import com.rtcomops.treasury.entity.BankAccount;
import com.rtcomops.treasury.enums.AuditAction;
import com.rtcomops.treasury.enums.AuditModule;
import com.rtcomops.treasury.exception.DuplicateResourceException;
import com.rtcomops.treasury.exception.ResourceNotFoundException;
import com.rtcomops.treasury.mapper.BankAccountMapper;
import com.rtcomops.treasury.repository.BankAccountRepository;
import com.rtcomops.treasury.repository.BankRepository;
import com.rtcomops.treasury.service.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service layer for BankAccount operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Service
@Transactional
public class BankAccountService {

    private static final Logger LOG = LoggerFactory.getLogger(BankAccountService.class);
    private static final String RESOURCE_NAME = "BankAccount";

    private final BankAccountRepository bankAccountRepository;
    private final BankRepository bankRepository;
    private final BankAccountMapper bankAccountMapper;
    private final AuditLogService auditLogService;

    public BankAccountService(
            BankAccountRepository bankAccountRepository,
            BankRepository bankRepository,
            BankAccountMapper bankAccountMapper,
            AuditLogService auditLogService) {
        this.bankAccountRepository = bankAccountRepository;
        this.bankRepository = bankRepository;
        this.bankAccountMapper = bankAccountMapper;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Flux<BankAccountResponse> findAll(boolean activeOnly) {
        LOG.debug("Finding all bank accounts, activeOnly={}", activeOnly);
        
        Flux<BankAccount> accounts = activeOnly 
            ? bankAccountRepository.findAllActiveOrderByName()
            : bankAccountRepository.findAllOrderByName();
        
        return accounts.flatMap(this::enrichWithBankName);
    }

    @Transactional(readOnly = true)
    public Mono<BankAccountResponse> findById(UUID id) {
        LOG.debug("Finding bank account by id={}", id);
        
        return bankAccountRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(this::enrichWithBankName);
    }

    @Transactional(readOnly = true)
    public Flux<BankAccountResponse> findByBankId(UUID bankId) {
        LOG.debug("Finding bank accounts by bankId={}", bankId);
        
        return bankAccountRepository.findByBankId(bankId)
            .flatMap(this::enrichWithBankName);
    }

    public Mono<BankAccountResponse> create(CreateBankAccountRequest request) {
        LOG.info("Creating bank account with name={}", request.getName());
        
        return bankRepository.findById(request.getBankId())
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Bank", request.getBankId())))
            .flatMap(bank -> validateUniqueFields(request.getAccountNumber(), request.getIban(), null)
                .then(Mono.defer(() -> {
                    BankAccount entity = bankAccountMapper.toEntity(request);
                    return bankAccountRepository.save(entity)
                        .flatMap(saved -> auditLogService.log(
                                AuditModule.BANK_ACCOUNT,
                                AuditAction.CREATE,
                                saved.getId(),
                                saved.getAccountNumber(),
                                null,
                                saved,
                                "Création du compte bancaire: " + saved.getName()
                        ).thenReturn(saved))
                        .doOnSuccess(saved -> LOG.info("Bank account created: id={}", saved.getId()))
                        .map(saved -> bankAccountMapper.toResponseWithBankName(saved, bank.getName()));
                })));
    }

    public Mono<BankAccountResponse> update(UUID id, UpdateBankAccountRequest request) {
        LOG.info("Updating bank account id={}", id);
        
        return bankAccountRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                Mono<Void> validations = Mono.empty();
                
                if (request.getBankId() != null && !request.getBankId().equals(existing.getBankId())) {
                    validations = validations.then(
                        bankRepository.findById(request.getBankId())
                            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Bank", request.getBankId())))
                            .then()
                    );
                }
                
                if (request.getAccountNumber() != null || request.getIban() != null) {
                    validations = validations.then(
                        validateUniqueFieldsForUpdate(
                            request.getAccountNumber(), 
                            request.getIban(), 
                            id, 
                            existing)
                    );
                }
                
                return validations.then(Mono.defer(() -> {
                    BankAccount updated = bankAccountMapper.updateEntity(existing, request);
                    return bankAccountRepository.save(updated)
                            .flatMap(saved -> auditLogService.log(
                                    AuditModule.BANK_ACCOUNT,
                                    AuditAction.UPDATE,
                                    saved.getId(),
                                    saved.getAccountNumber(),
                                    existing,
                                    saved,
                                    "Mise à jour du compte bancaire: " + saved.getName()
                            ).thenReturn(saved))
                        .flatMap(this::enrichWithBankName);
                }));
            });
    }

    public Mono<Void> delete(UUID id) {
        LOG.info("Deleting bank account id={}", id);
        
        return bankAccountRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(account -> auditLogService.log(
                    AuditModule.BANK_ACCOUNT,
                    AuditAction.DELETE,
                    account.getId(),
                    account.getAccountNumber(),
                    account,
                    null,
                    "Suppression du compte bancaire: " + account.getName()
                ).then(bankAccountRepository.delete(account).then(Mono.fromRunnable(() -> LOG.info("Bank account deleted: id={}", id))))
            );
    }

    private Mono<BankAccountResponse> enrichWithBankName(BankAccount account) {
        return bankRepository.findById(account.getBankId())
            .map(bank -> bankAccountMapper.toResponseWithBankName(account, bank.getName()))
            .defaultIfEmpty(bankAccountMapper.toResponse(account));
    }

    private Mono<Void> validateUniqueFields(String accountNumber, String iban, UUID excludeId) {
        Mono<Void> accountNumberCheck = Mono.empty();
        Mono<Void> ibanCheck = Mono.empty();
        
        if (accountNumber != null) {
            accountNumberCheck = bankAccountRepository.existsByAccountNumber(accountNumber)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateResourceException(
                            RESOURCE_NAME, "accountNumber", accountNumber));
                    }
                    return Mono.empty();
                });
        }
        
        if (iban != null && !iban.isEmpty()) {
            ibanCheck = bankAccountRepository.existsByIban(iban)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateResourceException(
                            RESOURCE_NAME, "iban", iban));
                    }
                    return Mono.empty();
                });
        }
        
        return accountNumberCheck.then(ibanCheck);
    }

    private Mono<Void> validateUniqueFieldsForUpdate(
            String accountNumber, String iban, UUID id, BankAccount existing) {
        Mono<Void> accountNumberCheck = Mono.empty();
        Mono<Void> ibanCheck = Mono.empty();
        
        if (accountNumber != null && !accountNumber.equals(existing.getAccountNumber())) {
            accountNumberCheck = bankAccountRepository.existsByAccountNumberAndIdNot(accountNumber, id)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateResourceException(
                            RESOURCE_NAME, "accountNumber", accountNumber));
                    }
                    return Mono.empty();
                });
        }
        
        if (iban != null && !iban.isEmpty() && !iban.equals(existing.getIban())) {
            ibanCheck = bankAccountRepository.existsByIbanAndIdNot(iban, id)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateResourceException(
                            RESOURCE_NAME, "iban", iban));
                    }
                    return Mono.empty();
                });
        }
        
        return accountNumberCheck.then(ibanCheck);
    }
}
