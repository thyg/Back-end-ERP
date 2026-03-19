package com.rtcomops.treasury.application.service;

import com.rtcomops.treasury.application.dto.request.CreateBankTransactionRequest;
import com.rtcomops.treasury.application.dto.request.UpdateBankTransactionRequest;
import com.rtcomops.treasury.application.dto.response.BankTransactionResponse;
import com.rtcomops.treasury.application.mapper.BankTransactionMapper;
import com.rtcomops.treasury.domain.exception.BusinessException;
import com.rtcomops.treasury.domain.exception.ResourceNotFoundException;
import com.rtcomops.treasury.domain.model.BankAccount;
import com.rtcomops.treasury.domain.model.BankTransaction;
import com.rtcomops.treasury.domain.model.Check;
import com.rtcomops.treasury.domain.model.enums.AuditAction;
import com.rtcomops.treasury.domain.model.enums.AuditModule;
import com.rtcomops.treasury.application.port.in.AuditLogUseCase;
import com.rtcomops.treasury.application.port.in.BankTransactionUseCase;
import com.rtcomops.treasury.domain.port.out.BankAccountRepositoryPort;
import com.rtcomops.treasury.domain.port.out.BankTransactionRepositoryPort;
import com.rtcomops.treasury.domain.port.out.CheckRepositoryPort;
import com.rtcomops.treasury.domain.port.out.TransactionSequenceRepositoryPort;
import com.rtcomops.treasury.domain.port.out.TransactionTypeRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Domain service implementing BankTransaction use cases.
 *
 * <p>This service contains the business logic for bank transaction operations
 * including creation, validation, cancellation, and balance updates.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Service
@Transactional
public class BankTransactionService implements BankTransactionUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(BankTransactionService.class);
    private static final String RESOURCE_NAME = "BankTransaction";
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final BankTransactionRepositoryPort transactionRepository;
    private final BankAccountRepositoryPort accountRepository;
    private final TransactionTypeRepositoryPort typeRepository;
    private final TransactionSequenceRepositoryPort sequenceRepository;
    private final BankTransactionMapper transactionMapper;
    private final AuditLogUseCase auditLogUseCase;
    private final CheckRepositoryPort checkRepository;

    /**
     * Constructs the service with required dependencies.
     *
     * @param transactionRepository the transaction repository port
     * @param accountRepository the account repository port
     * @param typeRepository the transaction type repository port
     * @param sequenceRepository the sequence repository port
     * @param transactionMapper the transaction mapper
     * @param auditLogUseCase the audit log use case
     * @param checkRepository the check repository port
     */
    public BankTransactionService(
            BankTransactionRepositoryPort transactionRepository,
            BankAccountRepositoryPort accountRepository,
            TransactionTypeRepositoryPort typeRepository,
            TransactionSequenceRepositoryPort sequenceRepository,
            BankTransactionMapper transactionMapper,
            AuditLogUseCase auditLogUseCase,
            CheckRepositoryPort checkRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.typeRepository = typeRepository;
        this.sequenceRepository = sequenceRepository;
        this.transactionMapper = transactionMapper;
        this.auditLogUseCase = auditLogUseCase;
        this.checkRepository = checkRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Flux<BankTransactionResponse> findAll() {
        LOG.debug("Finding all bank transactions");
        return transactionRepository.findAllOrderByDateDesc()
            .flatMap(this::enrichWithDetails);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Mono<BankTransactionResponse> findById(UUID id) {
        LOG.debug("Finding bank transaction by id={}", id);
        return transactionRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(this::enrichWithDetails);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Flux<BankTransactionResponse> findByAccountId(UUID accountId) {
        LOG.debug("Finding transactions by accountId={}", accountId);
        return transactionRepository.findByBankAccountId(accountId)
            .flatMap(this::enrichWithDetails);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Flux<BankTransactionResponse> findByAccountIdAndDateRange(
            UUID accountId, LocalDate startDate, LocalDate endDate) {
        LOG.debug("Finding transactions by accountId={} between {} and {}", accountId, startDate, endDate);
        return transactionRepository.findByBankAccountIdAndDateRange(accountId, startDate, endDate)
            .flatMap(this::enrichWithDetails);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Flux<BankTransactionResponse> findByStatus(String status) {
        LOG.debug("Finding transactions by status={}", status);
        return transactionRepository.findByStatus(status.toUpperCase())
            .flatMap(this::enrichWithDetails);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<BankTransactionResponse> create(CreateBankTransactionRequest request) {
        LOG.info("Creating bank transaction for account={}, checkId={}",
            request.getBankAccountId(), request.getCheckId());

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
                                        updateAccountBalance(tx.getBankAccountId(), tx.getAmount(),
                                            tx.getDirection(), true)
                                            .thenReturn(tx)
                                    );
                                }

                                return pipeline;
                            })
                            .flatMap(finalTx -> auditLogUseCase.log(
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
                check.setUpdatedAt(LocalDateTime.now());

                return checkRepository.save(check)
                    .doOnSuccess(saved -> LOG.info("Linked check {} to transaction {}, status -> CASHED",
                        saved.getId(), transaction.getReference()))
                    .flatMap(savedCheck -> auditLogUseCase.log(
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

        return sequenceRepository.incrementSequence(normalizedCode, yearMonth)
            .then(sequenceRepository.findLastSequence(normalizedCode, yearMonth))
            .map(sequence -> {
                String formattedSequence = String.format("%04d", sequence);
                return normalizedCode + "-" + yearMonth + "-" + formattedSequence;
            })
            .switchIfEmpty(Mono.error(new BusinessException(
                "Impossible de générer une référence. La séquence n'a pas été trouvée après incrémentation."
            )));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<BankTransactionResponse> update(UUID id, UpdateBankTransactionRequest request) {
        LOG.info("Updating bank transaction id={}", id);

        return transactionRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                // Copier l'état précédent pour l'audit
                BankTransaction oldState = transactionMapper.copy(existing);

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
                        .flatMap(saved -> auditLogUseCase.log(
                            AuditModule.BANK_TRANSACTION,
                            AuditAction.UPDATE,
                            saved.getId(),
                            "Transaction " + saved.getReference(),
                            oldState,
                            saved,
                            "Mise à jour de la transaction " + saved.getReference()
                        ).then(enrichWithDetails(saved)));
                }));
            });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> delete(UUID id) {
        LOG.info("Deleting bank transaction id={}", id);

        return transactionRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(transaction ->
                auditLogUseCase.log(
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<BankTransactionResponse> validate(UUID id) {
        LOG.info("Validating bank transaction id={}", id);

        return transactionRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                if (!"DRAFT".equals(existing.getStatus())) {
                    return Mono.error(new BusinessException(
                        "Seules les transactions en brouillon peuvent être validées"));
                }

                // Copier l'état précédent pour l'audit
                BankTransaction oldState = transactionMapper.copy(existing);
                existing.setStatus("VALIDATED");

                return transactionRepository.save(existing)
                    .flatMap(saved ->
                        updateAccountBalance(saved.getBankAccountId(), saved.getAmount(),
                                saved.getDirection(), true)
                            .then(auditLogUseCase.log(
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
     * Updates the bank account balance incrementally.
     *
     * @param accountId the bank account ID
     * @param amount the transaction amount
     * @param direction the transaction direction (CREDIT or DEBIT)
     * @param isAddition true to add to balance (validation), false to subtract (cancellation)
     * @return Mono that completes when the update is done
     */
    private Mono<Void> updateAccountBalance(UUID accountId, BigDecimal amount,
            String direction, boolean isAddition) {
        return accountRepository.findById(accountId)
            .flatMap(account -> {
                BigDecimal currentBalance = account.getCurrentBalance() != null
                    ? account.getCurrentBalance()
                    : BigDecimal.ZERO;

                BigDecimal adjustment;
                if ("CREDIT".equals(direction)) {
                    adjustment = amount;
                } else {
                    adjustment = amount.negate();
                }

                if (!isAddition) {
                    adjustment = adjustment.negate();
                }

                BigDecimal newBalance = currentBalance.add(adjustment);
                account.setCurrentBalance(newBalance);

                LOG.info("Updating account {} balance: {} {} {} = {}",
                    accountId, currentBalance, isAddition ? "+" : "-",
                    adjustment.abs(), newBalance);
                return accountRepository.save(account).then();
            });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<BankTransactionResponse> cancel(UUID id) {
        LOG.info("Cancelling bank transaction id={}", id);

        return transactionRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                if ("CANCELLED".equals(existing.getStatus())) {
                    return Mono.error(new BusinessException(
                        "La transaction est déjà annulée"));
                }

                if (Boolean.TRUE.equals(existing.getIsReconciled())) {
                    return Mono.error(new BusinessException(
                        "Impossible d'annuler une transaction rapprochée"));
                }

                boolean wasValidated = "VALIDATED".equals(existing.getStatus());
                // Copier l'état précédent pour l'audit
                BankTransaction oldState = transactionMapper.copy(existing);
                existing.setStatus("CANCELLED");

                return transactionRepository.save(existing)
                    .flatMap(saved -> {
                        Mono<Void> updateBalance = wasValidated
                            ? updateAccountBalance(saved.getBankAccountId(), saved.getAmount(),
                                    saved.getDirection(), false)
                            : Mono.empty();

                        return updateBalance
                            .then(auditLogUseCase.log(
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

    /**
     * Enriches a transaction with account and type details.
     *
     * @param transaction the transaction to enrich
     * @return Mono containing the enriched response
     */
    private Mono<BankTransactionResponse> enrichWithDetails(BankTransaction transaction) {
        return Mono.zip(
            accountRepository.findById(transaction.getBankAccountId())
                .map(BankAccount::getName)
                .defaultIfEmpty("Unknown"),
            typeRepository.findById(transaction.getTransactionTypeId())
                .map(t -> new String[]{t.getCode(), t.getLabel()})
                .defaultIfEmpty(new String[]{"Unknown", "Unknown"})
        ).map(tuple -> transactionMapper.toResponseWithDetails(
            transaction, tuple.getT1(), tuple.getT2()[0], tuple.getT2()[1]));
    }
}
