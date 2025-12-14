package com.rtcomops.treasury.service;

import com.rtcomops.treasury.dto.request.CreateBankTransactionRequest;
import com.rtcomops.treasury.dto.request.UpdateBankTransactionRequest;
import com.rtcomops.treasury.dto.response.BankTransactionResponse;
import com.rtcomops.treasury.entity.BankTransaction;
import com.rtcomops.treasury.enums.AuditAction;
import com.rtcomops.treasury.enums.AuditModule;
import com.rtcomops.treasury.exception.ResourceNotFoundException;
import com.rtcomops.treasury.mapper.BankTransactionMapper;
import com.rtcomops.treasury.repository.BankAccountRepository;
import com.rtcomops.treasury.repository.BankTransactionRepository;
import com.rtcomops.treasury.repository.TransactionTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Service layer for BankTransaction operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Service
@Transactional
public class BankTransactionService {

    private static final Logger LOG = LoggerFactory.getLogger(BankTransactionService.class);
    private static final String RESOURCE_NAME = "BankTransaction";

    private final BankTransactionRepository transactionRepository;
    private final BankAccountRepository accountRepository;
    private final TransactionTypeRepository typeRepository;
    private final BankTransactionMapper transactionMapper;
    private final AuditLogService auditLogService;

    public BankTransactionService(
            BankTransactionRepository transactionRepository,
            BankAccountRepository accountRepository,
            TransactionTypeRepository typeRepository,
            BankTransactionMapper transactionMapper,
            AuditLogService auditLogService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.typeRepository = typeRepository;
        this.transactionMapper = transactionMapper;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Flux<BankTransactionResponse> findAll() {
        LOG.debug("Finding all bank transactions");
        return transactionRepository.findAllOrderByDateDesc()
            .flatMap(this::enrichWithDetails);
    }

    @Transactional(readOnly = true)
    public Mono<BankTransactionResponse> findById(UUID id) {
        LOG.debug("Finding bank transaction by id={}", id);
        return transactionRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(this::enrichWithDetails);
    }

    @Transactional(readOnly = true)
    public Flux<BankTransactionResponse> findByAccountId(UUID accountId) {
        LOG.debug("Finding transactions by accountId={}", accountId);
        return transactionRepository.findByBankAccountId(accountId)
            .flatMap(this::enrichWithDetails);
    }

    @Transactional(readOnly = true)
    public Flux<BankTransactionResponse> findByAccountIdAndDateRange(
            UUID accountId, LocalDate startDate, LocalDate endDate) {
        LOG.debug("Finding transactions by accountId={} between {} and {}", accountId, startDate, endDate);
        return transactionRepository.findByBankAccountIdAndDateRange(accountId, startDate, endDate)
            .flatMap(this::enrichWithDetails);
    }

    @Transactional(readOnly = true)
    public Flux<BankTransactionResponse> findByStatus(String status) {
        LOG.debug("Finding transactions by status={}", status);
        return transactionRepository.findByStatus(status.toUpperCase())
            .flatMap(this::enrichWithDetails);
    }

    public Mono<BankTransactionResponse> create(CreateBankTransactionRequest request) {
        LOG.info("Creating bank transaction for account={}", request.getBankAccountId());
        
        return accountRepository.findById(request.getBankAccountId())
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankAccount", request.getBankAccountId())))
            .flatMap(account -> typeRepository.findById(request.getTransactionTypeId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("TransactionType", request.getTransactionTypeId())))
                .flatMap(type -> {
                    BankTransaction entity = transactionMapper.toEntity(request);
                    return transactionRepository.save(entity)
                        .doOnSuccess(saved -> LOG.info("Bank transaction created: id={}", saved.getId()))
                        .flatMap(saved -> auditLogService.log(
                                AuditModule.BANK_TRANSACTION,
                                AuditAction.CREATE,
                                saved.getId(),
                                "Transaction " + saved.getReference(),
                                null,
                                saved,
                                "Création de la transaction " + saved.getReference()
                            ).then(enrichWithDetails(saved))
                        );
                }));
    }

    public Mono<BankTransactionResponse> update(UUID id, UpdateBankTransactionRequest request) {
        LOG.info("Updating bank transaction id={}", id);
        
        return transactionRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                Mono<Void> typeValidation = Mono.empty();
                if (request.getTransactionTypeId() != null && 
                    !request.getTransactionTypeId().equals(existing.getTransactionTypeId())) {
                    typeValidation = typeRepository.findById(request.getTransactionTypeId())
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                            "TransactionType", request.getTransactionTypeId())))
                        .then();
                }
                
                return typeValidation.then(Mono.defer(() -> {
                    BankTransaction updated = transactionMapper.updateEntity(existing, request);
                    return transactionRepository.save(updated)
                            .flatMap(saved -> auditLogService.log(
                                    AuditModule.BANK_TRANSACTION,
                                    AuditAction.UPDATE,
                                    saved.getId(),
                                    "Transaction " + saved.getReference(),
                                    existing,
                                    saved,
                                    "Mise à jour de la transaction " + saved.getReference()
                            ).then(enrichWithDetails(saved)));
                }));
            });
    }

    public Mono<Void> delete(UUID id) {
        LOG.info("Deleting bank transaction id={}", id);
        
        return transactionRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(transaction ->
                auditLogService.log(
                    AuditModule.BANK_TRANSACTION,
                    AuditAction.DELETE,
                    transaction.getId(),
                    "Transaction " + transaction.getReference(),
                    transaction,
                    null,
                    "Suppression de la transaction " + transaction.getReference()
                ).then(transactionRepository.delete(transaction))
                    .doOnSuccess(v -> LOG.info("Bank transaction deleted: id={}", id))
            );
    }

    public Mono<BankTransactionResponse> validate(UUID id) {
        LOG.info("Validating bank transaction id={}", id);
        
        return transactionRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                BankTransaction oldState = transactionMapper.copy(existing);
                existing.setStatus("VALIDATED");
                existing.setNew(false);
                return transactionRepository.save(existing)
                    .flatMap(saved -> auditLogService.log(
                        AuditModule.BANK_TRANSACTION,
                        AuditAction.UPDATE,
                        saved.getId(),
                        "Transaction " + saved.getReference(),
                        oldState,
                        saved,
                        "Validation de la transaction " + saved.getReference()
                    ).then(enrichWithDetails(saved)));
            });
    }

    public Mono<BankTransactionResponse> cancel(UUID id) {
        LOG.info("Cancelling bank transaction id={}", id);
        
        return transactionRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                BankTransaction oldState = transactionMapper.copy(existing);
                existing.setStatus("CANCELLED");
                existing.setNew(false);
                return transactionRepository.save(existing)
                    .flatMap(saved -> auditLogService.log(
                        AuditModule.BANK_TRANSACTION,
                        AuditAction.UPDATE,
                        saved.getId(),
                        "Transaction " + saved.getReference(),
                        oldState,
                        saved,
                        "Annulation de la transaction " + saved.getReference()
                    ).then(enrichWithDetails(saved)));
            });
    }

    private Mono<BankTransactionResponse> enrichWithDetails(BankTransaction transaction) {
        return Mono.zip(
            accountRepository.findById(transaction.getBankAccountId())
                .map(a -> a.getName())
                .defaultIfEmpty("Unknown"),
            typeRepository.findById(transaction.getTransactionTypeId())
                .map(t -> new String[]{t.getCode(), t.getLabel()})
                .defaultIfEmpty(new String[]{"Unknown", "Unknown"})
        ).map(tuple -> transactionMapper.toResponseWithDetails(
            transaction, tuple.getT1(), tuple.getT2()[0], tuple.getT2()[1]));
    }
}