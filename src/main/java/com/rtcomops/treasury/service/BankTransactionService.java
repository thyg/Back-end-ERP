package com.rtcomops.treasury.service;

import com.rtcomops.treasury.dto.request.CreateBankTransactionRequest;
import com.rtcomops.treasury.dto.request.UpdateBankTransactionRequest;
import com.rtcomops.treasury.dto.response.BankTransactionResponse;
import com.rtcomops.treasury.entity.BankTransaction;
import com.rtcomops.treasury.entity.Check;
import com.rtcomops.treasury.enums.AuditAction;
import com.rtcomops.treasury.enums.AuditModule;
import com.rtcomops.treasury.exception.BusinessException;
import com.rtcomops.treasury.exception.ResourceNotFoundException;
import com.rtcomops.treasury.mapper.BankTransactionMapper;
import com.rtcomops.treasury.repository.BankAccountRepository;
import com.rtcomops.treasury.repository.BankTransactionRepository;
import com.rtcomops.treasury.repository.CheckRepository;
import com.rtcomops.treasury.repository.TransactionSequenceRepository;
import com.rtcomops.treasury.repository.TransactionTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final BankTransactionRepository transactionRepository;
    private final BankAccountRepository accountRepository;
    private final TransactionTypeRepository typeRepository;
    private final TransactionSequenceRepository sequenceRepository;
    private final BankTransactionMapper transactionMapper;
    private final AuditLogService auditLogService;
    private final CheckRepository checkRepository;

    public BankTransactionService(
            BankTransactionRepository transactionRepository,
            BankAccountRepository accountRepository,
            TransactionTypeRepository typeRepository,
            TransactionSequenceRepository sequenceRepository,
            BankTransactionMapper transactionMapper,
            AuditLogService auditLogService,
            CheckRepository checkRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.typeRepository = typeRepository;
        this.sequenceRepository = sequenceRepository;
        this.transactionMapper = transactionMapper;
        this.auditLogService = auditLogService;
        this.checkRepository = checkRepository;
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
        LOG.info("Creating bank transaction for account={}, checkId={}", request.getBankAccountId(), request.getCheckId());

        return accountRepository.findById(request.getBankAccountId())
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankAccount", request.getBankAccountId())))
            .flatMap(account -> typeRepository.findById(request.getTransactionTypeId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("TransactionType", request.getTransactionTypeId())))
                .flatMap(type -> generateReference(type.getCode())
                    .flatMap(reference -> {
                        BankTransaction entity = transactionMapper.toEntity(request);
                        entity.setReference(reference);

                        // Si un checkId est fourni, la transaction est validée d'office
                        if (request.getCheckId() != null) {
                            entity.setStatus("VALIDATED");
                        }

                        return transactionRepository.save(entity)
                            .doOnSuccess(saved -> LOG.info("Bank transaction created: id={}, reference={}",
                                saved.getId(), saved.getReference()))
                            .flatMap(saved -> {
                                Mono<BankTransaction> pipeline = Mono.just(saved);

                                // Si un checkId est lié, mettre à jour le chèque
                                if (request.getCheckId() != null) {
                                    pipeline = pipeline.flatMap(tx ->
                                        linkAndUpdateCheck(tx, request.getCheckId())
                                            .thenReturn(tx)
                                    );
                                }

                                // Si la transaction est validée, mettre à jour le solde du compte
                                if ("VALIDATED".equals(saved.getStatus())) {
                                    pipeline = pipeline.flatMap(tx ->
                                        updateAccountBalance(tx.getBankAccountId(), tx.getAmount(), tx.getDirection(), true)
                                            .thenReturn(tx)
                                    );
                                }

                                return pipeline;
                            })
                            .flatMap(finalTx -> auditLogService.log(
                                AuditModule.BANK_TRANSACTION,
                                AuditAction.CREATE,
                                finalTx.getId(),
                                "Transaction " + finalTx.getReference(),
                                null,
                                finalTx,
                                "Création de la transaction " + finalTx.getReference()
                            ).then(enrichWithDetails(finalTx)));
                    })
                ));
    }

    /**
     * Generates an automatic reference for a transaction.
     * Format: CODE-YYYYMM-NNNN (e.g., VIREMENT-202412-0001)
     *
     * @param typeCode the transaction type code
     * @return the generated reference
     */
    private Mono<String> generateReference(String typeCode) {
        String normalizedCode = typeCode.toUpperCase().replace("_", "-");
        String yearMonth = LocalDate.now().format(YEAR_MONTH_FORMATTER);

        return sequenceRepository.getNextSequence(normalizedCode, yearMonth)
            .map(sequence -> {
                String formattedSequence = String.format("%04d", sequence);
                return normalizedCode + "-" + yearMonth + "-" + formattedSequence;
            });
    }

    /**
     * Lie un chèque à une transaction et met à jour son statut à CASHED.
     *
     * @param transaction la transaction à laquelle lier le chèque
     * @param checkId l'ID du chèque à lier
     * @return Mono<Check> le chèque mis à jour
     */
    private Mono<Check> linkAndUpdateCheck(BankTransaction transaction, UUID checkId) {
        return checkRepository.findById(checkId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Check", checkId)))
            .flatMap(check -> {
                // Vérifier que le chèque n'est pas déjà traité
                if ("CASHED".equals(check.getStatus()) || "CANCELLED".equals(check.getStatus())) {
                    return Mono.error(new BusinessException(
                        "Ce chèque a déjà été traité et ne peut être lié à une transaction."));
                }

                // Copier l'état précédent pour l'audit
                Check oldCheckState = check.toBuilder().build();

                // Mettre à jour le chèque
                check.setBankTransactionId(transaction.getId());
                check.setStatus("CASHED");
                check.setCashedDate(transaction.getTransactionDate());
                check.setNew(false);

                return checkRepository.save(check)
                    .doOnSuccess(saved -> LOG.info("Linked check {} to transaction {}, status -> CASHED",
                        saved.getId(), transaction.getReference()))
                    .flatMap(savedCheck -> auditLogService.log(
                        AuditModule.CHECK,
                        AuditAction.UPDATE,
                        savedCheck.getId(),
                        savedCheck.getCheckNumber(),
                        oldCheckState,
                        savedCheck,
                        "Chèque lié à la transaction " + transaction.getReference()
                    ).thenReturn(savedCheck));
            });
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
                // Vérifier que la transaction est en brouillon
                if (!"DRAFT".equals(existing.getStatus())) {
                    return Mono.error(new com.rtcomops.treasury.exception.BusinessException(
                        "Seules les transactions en brouillon peuvent être validées"));
                }

                BankTransaction oldState = transactionMapper.copy(existing);
                existing.setStatus("VALIDATED");
                existing.setNew(false);

                return transactionRepository.save(existing)
                    .flatMap(saved ->
                        // Mettre à jour le solde du compte (ajouter la transaction)
                        updateAccountBalance(saved.getBankAccountId(), saved.getAmount(),
                                saved.getDirection(), true)
                            .then(auditLogService.log(
                                AuditModule.BANK_TRANSACTION,
                                AuditAction.VALIDATE,
                                saved.getId(),
                                "Transaction " + saved.getReference(),
                                oldState,
                                saved,
                                "Validation de la transaction " + saved.getReference()
                            ))
                            .then(enrichWithDetails(saved))
                    );
            });
    }

    /**
     * Met à jour le solde du compte bancaire de manière incrémentale.
     *
     * @param accountId l'ID du compte bancaire
     * @param amount le montant de la transaction
     * @param direction le sens de la transaction (CREDIT ou DEBIT)
     * @param isAddition true pour ajouter au solde (validation), false pour soustraire (annulation)
     * @return Mono<Void>
     */
    private Mono<Void> updateAccountBalance(UUID accountId, java.math.BigDecimal amount,
            String direction, boolean isAddition) {
        return accountRepository.findById(accountId)
            .flatMap(account -> {
                java.math.BigDecimal currentBalance = account.getCurrentBalance() != null
                    ? account.getCurrentBalance()
                    : java.math.BigDecimal.ZERO;

                java.math.BigDecimal adjustment;
                if ("CREDIT".equals(direction)) {
                    adjustment = amount;
                } else {
                    adjustment = amount.negate();
                }

                // Si c'est une annulation, on inverse l'ajustement
                if (!isAddition) {
                    adjustment = adjustment.negate();
                }

                java.math.BigDecimal newBalance = currentBalance.add(adjustment);
                account.setCurrentBalance(newBalance);
                account.setNew(false);

                LOG.info("Updating account {} balance: {} {} {} = {}",
                    accountId, currentBalance, isAddition ? "+" : "-",
                    adjustment.abs(), newBalance);
                return accountRepository.save(account).then();
            });
    }

    public Mono<BankTransactionResponse> cancel(UUID id) {
        LOG.info("Cancelling bank transaction id={}", id);

        return transactionRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                // Vérifier que la transaction n'est pas déjà annulée
                if ("CANCELLED".equals(existing.getStatus())) {
                    return Mono.error(new com.rtcomops.treasury.exception.BusinessException(
                        "La transaction est déjà annulée"));
                }

                // Vérifier que la transaction n'est pas rapprochée
                if (Boolean.TRUE.equals(existing.getIsReconciled())) {
                    return Mono.error(new com.rtcomops.treasury.exception.BusinessException(
                        "Impossible d'annuler une transaction rapprochée"));
                }

                boolean wasValidated = "VALIDATED".equals(existing.getStatus());
                BankTransaction oldState = transactionMapper.copy(existing);
                existing.setStatus("CANCELLED");
                existing.setNew(false);

                return transactionRepository.save(existing)
                    .flatMap(saved -> {
                        // Si la transaction était validée, annuler l'effet sur le solde
                        Mono<Void> updateBalance = wasValidated
                            ? updateAccountBalance(saved.getBankAccountId(), saved.getAmount(),
                                    saved.getDirection(), false)
                            : Mono.empty();

                        return updateBalance
                            .then(auditLogService.log(
                                AuditModule.BANK_TRANSACTION,
                                AuditAction.CANCEL,
                                saved.getId(),
                                "Transaction " + saved.getReference(),
                                oldState,
                                saved,
                                "Annulation de la transaction " + saved.getReference()
                            ))
                            .then(enrichWithDetails(saved));
                    });
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