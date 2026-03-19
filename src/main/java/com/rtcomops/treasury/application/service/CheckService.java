package com.rtcomops.treasury.application.service;

import com.rtcomops.treasury.application.dto.request.CreateCheckRequest;
import com.rtcomops.treasury.application.dto.request.UpdateCheckRequest;
import com.rtcomops.treasury.application.dto.response.CheckResponse;
import com.rtcomops.treasury.application.dto.response.CheckStatsResponse;
import com.rtcomops.treasury.application.mapper.CheckMapper;
import com.rtcomops.treasury.domain.exception.BusinessException;
import com.rtcomops.treasury.domain.exception.DuplicateResourceException;
import com.rtcomops.treasury.domain.exception.ResourceNotFoundException;
import com.rtcomops.treasury.domain.model.BankAccount;
import com.rtcomops.treasury.domain.model.BankTransaction;
import com.rtcomops.treasury.domain.model.Check;
import com.rtcomops.treasury.domain.model.Checkbook;
import com.rtcomops.treasury.domain.model.TransactionType;
import com.rtcomops.treasury.domain.model.enums.AuditAction;
import com.rtcomops.treasury.domain.model.enums.AuditModule;
import com.rtcomops.treasury.application.port.in.AuditLogUseCase;
import com.rtcomops.treasury.application.port.in.BalanceValidationUseCase;
import com.rtcomops.treasury.application.port.in.CheckUseCase;
import com.rtcomops.treasury.domain.port.out.BankAccountRepositoryPort;
import com.rtcomops.treasury.domain.port.out.BankTransactionRepositoryPort;
import com.rtcomops.treasury.domain.port.out.CheckRepositoryPort;
import com.rtcomops.treasury.domain.port.out.CheckbookRepositoryPort;
import com.rtcomops.treasury.domain.port.out.TransactionSequenceRepositoryPort;
import com.rtcomops.treasury.domain.port.out.TransactionTypeRepositoryPort;
import com.rtcomops.treasury.domain.util.NumberToWordsConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
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
 * Domain service for Check operations.
 *
 * <p>Implements the CheckUseCase port and provides business logic
 * for managing checks, including CRUD operations with validation
 * and state transitions.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Service
@Transactional
public class CheckService implements CheckUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(CheckService.class);
    private static final String RESOURCE_NAME = "Check";
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final String CHECK_TYPE_CODE = "CHQ";

    private final CheckRepositoryPort checkRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final CheckbookRepositoryPort checkbookRepositoryPort;
    private final BankTransactionRepositoryPort bankTransactionRepositoryPort;
    private final TransactionTypeRepositoryPort transactionTypeRepositoryPort;
    private final TransactionSequenceRepositoryPort transactionSequenceRepositoryPort;
    private final BalanceValidationUseCase balanceValidationUseCase;
    private final AuditLogUseCase auditLogUseCase;
    private final CheckMapper checkMapper;

    public CheckService(
            CheckRepositoryPort checkRepositoryPort,
            BankAccountRepositoryPort bankAccountRepositoryPort,
            CheckbookRepositoryPort checkbookRepositoryPort,
            BankTransactionRepositoryPort bankTransactionRepositoryPort,
            TransactionTypeRepositoryPort transactionTypeRepositoryPort,
            TransactionSequenceRepositoryPort transactionSequenceRepositoryPort,
            BalanceValidationUseCase balanceValidationUseCase,
            AuditLogUseCase auditLogUseCase,
            CheckMapper checkMapper) {
        this.checkRepositoryPort = checkRepositoryPort;
        this.bankAccountRepositoryPort = bankAccountRepositoryPort;
        this.checkbookRepositoryPort = checkbookRepositoryPort;
        this.bankTransactionRepositoryPort = bankTransactionRepositoryPort;
        this.transactionTypeRepositoryPort = transactionTypeRepositoryPort;
        this.transactionSequenceRepositoryPort = transactionSequenceRepositoryPort;
        this.balanceValidationUseCase = balanceValidationUseCase;
        this.auditLogUseCase = auditLogUseCase;
        this.checkMapper = checkMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<CheckResponse> findAll(@Nullable UUID checkbookId) {
        Flux<Check> checks;
        if (checkbookId != null) {
            LOG.debug("Finding all checks for checkbookId={}", checkbookId);
            checks = checkRepositoryPort.findByCheckbookId(checkbookId);
        } else {
            LOG.debug("Finding all checks");
            checks = checkRepositoryPort.findAllOrderByDateDesc();
        }
        return checks.flatMap(this::enrichWithAccountName);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<CheckResponse> findById(UUID id) {
        LOG.debug("Finding check by id={}", id);
        return checkRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(this::enrichWithAccountName);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<CheckResponse> findByType(String checkType) {
        LOG.debug("Finding checks by type={}", checkType);
        return checkRepositoryPort.findByCheckType(checkType.toUpperCase())
            .flatMap(this::enrichWithAccountName);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<CheckResponse> findByStatus(String status) {
        LOG.debug("Finding checks by status={}", status);
        return checkRepositoryPort.findByStatus(status.toUpperCase())
            .flatMap(this::enrichWithAccountName);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<CheckResponse> findByTypeAndStatus(String checkType, String status) {
        LOG.debug("Finding checks by type={} and status={}", checkType, status);
        return checkRepositoryPort.findByCheckTypeAndStatus(checkType.toUpperCase(), status.toUpperCase())
            .flatMap(this::enrichWithAccountName);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<CheckResponse> findByAccountId(UUID accountId) {
        LOG.debug("Finding checks by accountId={}", accountId);
        return checkRepositoryPort.findByBankAccountId(accountId)
            .flatMap(this::enrichWithAccountName);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<CheckResponse> findPendingChecksDueBefore(LocalDate date) {
        LOG.debug("Finding pending checks due before {}", date);
        return checkRepositoryPort.findPendingChecksDueBefore(date)
            .flatMap(this::enrichWithAccountName);
    }

    @Override
    public Mono<CheckResponse> create(CreateCheckRequest request) {
        LOG.info("Creating check for account={}", request.getBankAccountId());

        return bankAccountRepositoryPort.findById(request.getBankAccountId())
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankAccount", request.getBankAccountId())))
            .flatMap(account -> {
                // Verification du solde pour les cheques EMIS uniquement
                if ("ISSUED".equalsIgnoreCase(request.getCheckType())) {
                    return balanceValidationUseCase.validateDebitOperation(
                            request.getBankAccountId(), request.getAmount())
                        .flatMap(balanceInfo -> {
                            // Log si utilisation du decouvert
                            if (balanceInfo.overdraftAuthorized()) {
                                BigDecimal projectedBalance = balanceInfo.currentBalance()
                                    .subtract(request.getAmount());
                                if (projectedBalance.compareTo(BigDecimal.ZERO) < 0) {
                                    LOG.info("Check {} will use overdraft: projected balance={}",
                                        request.getCheckNumber(), projectedBalance);
                                }
                            }

                            // Continuer avec la creation
                            if (request.getCheckbookId() != null) {
                                return createCheckFromCheckbook(request, account);
                            } else {
                                return createCheckManual(request, account);
                            }
                        });
                }

                // For RECEIVED checks, always use manual creation
                if ("RECEIVED".equalsIgnoreCase(request.getCheckType())) {
                    return createCheckManual(request, account);
                }

                // For ISSUED checks: if checkbookId is provided, auto-generate the number
                if (request.getCheckbookId() != null) {
                    return createCheckFromCheckbook(request, account);
                } else {
                    return createCheckManual(request, account);
                }
            });
    }

    /**
     * Creates a check with number auto-generated from a checkbook.
     */
    private Mono<CheckResponse> createCheckFromCheckbook(CreateCheckRequest request, BankAccount account) {
        return checkbookRepositoryPort.findById(request.getCheckbookId())
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Checkbook", request.getCheckbookId())))
            .flatMap(checkbook -> {
                // Validate checkbook belongs to the same account (only for real checkbooks)
                if (!checkbook.isFictif() && checkbook.getBankAccountId() != null
                    && !checkbook.getBankAccountId().equals(request.getBankAccountId())) {
                    return Mono.error(new BusinessException(
                        "Le chequier n'appartient pas au compte bancaire specifie"));
                }

                // Check if checkbook has available checks
                if (!checkbook.hasAvailableChecks()) {
                    return Mono.error(new BusinessException(
                        "Le chequier n'a plus de cheques disponibles"));
                }

                // Get the next check number
                String checkNumber = checkbook.getNextCheckNumber();

                // Increment the checkbook counter
                return checkbookRepositoryPort.incrementCurrentNumber(checkbook.getId())
                    .flatMap(rowsAffected -> {
                        if (rowsAffected == 0) {
                            return Mono.error(new BusinessException(
                                "Erreur lors de l'allocation du numero de cheque"));
                        }

                        // Create the check entity
                        Check entity = checkMapper.toEntity(request);
                        entity.setCheckNumber(checkNumber);

                        // Generate amount in words
                        String amountInWords = NumberToWordsConverter.convert(
                            request.getAmount(), account.getCurrency());
                        entity.setAmountInWords(amountInWords);

                        return saveCheck(entity, account.getName(), account.getCurrency(), checkbook.getPrefix());
                    });
            });
    }

    /**
     * Creates a check with manually provided check number.
     */
    private Mono<CheckResponse> createCheckManual(CreateCheckRequest request, BankAccount account) {
        // Verifier que le numero de cheque est fourni
        if (request.getCheckNumber() == null || request.getCheckNumber().isBlank()) {
            return Mono.error(new BusinessException(
                "Le numero de cheque est obligatoire pour une saisie manuelle"));
        }

        return checkRepositoryPort.existsByCheckNumberAndAccountAndType(
                request.getCheckNumber(), request.getBankAccountId(), request.getCheckType().toUpperCase())
            .flatMap(exists -> {
                if (exists) {
                    return Mono.error(new DuplicateResourceException(
                        RESOURCE_NAME, "checkNumber", request.getCheckNumber()));
                }

                Check entity = checkMapper.toEntity(request);

                // Generate amount in words
                String amountInWords = NumberToWordsConverter.convert(
                    request.getAmount(), account.getCurrency());
                entity.setAmountInWords(amountInWords);

                // For RECEIVED checks, assign to the system checkbook for tracking purposes
                if ("RECEIVED".equalsIgnoreCase(request.getCheckType())) {
                    if (request.getCheckbookId() != null) {
                        return checkbookRepositoryPort.findById(request.getCheckbookId())
                            .flatMap(checkbook -> {
                                entity.setCheckbookId(checkbook.getId());
                                LOG.debug("Assigned provided checkbook {} to received check {}",
                                    checkbook.getId(), entity.getCheckNumber());
                                return saveCheck(entity, account.getName(), account.getCurrency(), checkbook.getPrefix());
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                LOG.warn("Provided checkbook {} not found, proceeding without assignment",
                                    request.getCheckbookId());
                                return saveCheck(entity, account.getName(), account.getCurrency(), null);
                            }));
                    }

                    // Auto-assign the system checkbook
                    return checkbookRepositoryPort.findByIsSystemTrue()
                        .flatMap(systemCheckbook -> {
                            if (!"ACTIVE".equals(systemCheckbook.getStatus())) {
                                LOG.warn("System checkbook {} is not active (status={}), proceeding without assignment",
                                    systemCheckbook.getId(), systemCheckbook.getStatus());
                                return saveCheck(entity, account.getName(), account.getCurrency(), null);
                            }
                            entity.setCheckbookId(systemCheckbook.getId());
                            LOG.debug("Auto-assigned system checkbook {} to received check {}",
                                systemCheckbook.getId(), entity.getCheckNumber());
                            return saveCheck(entity, account.getName(), account.getCurrency(), systemCheckbook.getPrefix());
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            LOG.warn("System checkbook not found. Creating received check {} without checkbook assignment.",
                                entity.getCheckNumber());
                            return saveCheck(entity, account.getName(), account.getCurrency(), null);
                        }))
                        .onErrorResume(e -> {
                            LOG.error("Error while fetching system checkbook, proceeding without assignment: {}",
                                e.getMessage());
                            return saveCheck(entity, account.getName(), account.getCurrency(), null);
                        });
                }

                return saveCheck(entity, account.getName(), account.getCurrency(), null);
            });
    }

    /**
     * Saves the check and logs the audit.
     */
    private Mono<CheckResponse> saveCheck(Check entity, String accountName, String currency, String checkbookPrefix) {
        return checkRepositoryPort.save(entity)
            .flatMap(saved -> auditLogUseCase.log(
                    AuditModule.CHECK,
                    AuditAction.CREATE,
                    saved.getId(),
                    saved.getCheckNumber(),
                    null,
                    saved,
                    "Création du chèque n°" + saved.getCheckNumber()
            ).then(Mono.just(saved)))
            .doOnSuccess(saved -> LOG.info("Check created: id={}, number={}",
                saved.getId(), saved.getCheckNumber()))
            .map(saved -> checkMapper.toResponseWithDetails(saved, accountName, currency, checkbookPrefix));
    }

    @Override
    public Mono<CheckResponse> update(UUID id, UpdateCheckRequest request) {
        LOG.info("Updating check id={}", id);

        return checkRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                // Copier l'état précédent pour l'audit
                Check oldState = existing.toBuilder().build();

                Mono<Void> validation = Mono.empty();

                if (request.getCheckNumber() != null &&
                    !request.getCheckNumber().equals(existing.getCheckNumber())) {
                    validation = checkRepositoryPort.existsByCheckNumberAndAccountAndTypeAndIdNot(
                            request.getCheckNumber(),
                            existing.getBankAccountId(),
                            existing.getCheckType(),
                            id)
                        .flatMap(exists -> {
                            if (exists) {
                                return Mono.error(new DuplicateResourceException(
                                    RESOURCE_NAME, "checkNumber", request.getCheckNumber()));
                            }
                            return Mono.empty();
                        });
                }

                return validation.then(Mono.defer(() -> {
                    Check updated = checkMapper.updateEntity(existing, request);
                    return checkRepositoryPort.save(updated)
                        .flatMap(saved -> auditLogUseCase.log(
                            AuditModule.CHECK,
                            AuditAction.UPDATE,
                            saved.getId(),
                            saved.getCheckNumber(),
                            oldState,
                            saved,
                            "Mise à jour du chèque n°" + saved.getCheckNumber()
                        ).then(enrichWithAccountName(saved)));
                }));
            });
    }

    @Override
    public Mono<Void> delete(UUID id) {
        LOG.info("Deleting check id={}", id);

        return checkRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(check -> {
                if (!"PENDING".equals(check.getStatus())) {
                    return Mono.error(new BusinessException(
                        "Seul un cheque au statut 'En attente' peut etre supprime."));
                }
                return auditLogUseCase.log(
                        AuditModule.CHECK,
                        AuditAction.DELETE,
                        check.getId(),
                        check.getCheckNumber(),
                        check,
                        null,
                        "Suppression du chèque n°" + check.getCheckNumber()
                    )
                    .then(checkRepositoryPort.delete(check))
                    .doOnSuccess(v -> LOG.info("Check deleted successfully: id={}", id));
            });
    }

    @Override
    public Mono<CheckResponse> deposit(UUID id, LocalDate depositDate) {
        LOG.info("Depositing check id={} on date={}", id, depositDate);

        return checkRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                if (!"RECEIVED".equals(existing.getCheckType())) {
                    return Mono.error(new BusinessException(
                        "Seuls les cheques recus peuvent etre remis en banque"));
                }

                if (!"PENDING".equals(existing.getStatus()) && !"RECEIVED".equals(existing.getStatus())) {
                    return Mono.error(new BusinessException(
                        "Seuls les cheques en attente ou recus peuvent etre remis en banque"));
                }

                // Copier l'état précédent pour l'audit
                Check oldState = existing.toBuilder().build();

                existing.setStatus("DEPOSITED");
                existing.setDepositDate(depositDate);
                existing.setUpdatedAt(LocalDateTime.now());

                return checkRepositoryPort.save(existing)
                    .flatMap(saved -> auditLogUseCase.log(
                        AuditModule.CHECK,
                        AuditAction.DEPOSIT,
                        saved.getId(),
                        saved.getCheckNumber(),
                        oldState,
                        saved,
                        "Remise en banque du chèque n°" + saved.getCheckNumber()
                    ).then(enrichWithAccountName(saved)));
            });
    }

    @Override
    public Mono<CheckResponse> cash(UUID id, LocalDate cashedDate) {
        LOG.info("Cashing check id={} on date={}", id, cashedDate);

        return checkRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                if ("RECEIVED".equals(existing.getCheckType())) {
                    return Mono.error(new BusinessException(
                        "L'encaissement d'un cheque recu doit se faire via le processus de Remise de Cheques."));
                } else {
                    if (!"ISSUED".equals(existing.getStatus())) {
                        return Mono.error(new BusinessException(
                            "Un cheque emis doit etre au statut 'Emis' pour etre marque comme paye."));
                    }
                    return cashIssuedCheck(existing, cashedDate);
                }
            });
    }

    /**
     * Encaisse un chèque ISSUED (émis).
     * Pour les chèques émis, on crée toujours une BankTransaction et on met à jour le solde.
     */
    private Mono<CheckResponse> cashIssuedCheck(Check existing, LocalDate cashedDate) {
        Check original = Check.builder()
            .id(existing.getId())
            .checkNumber(existing.getCheckNumber())
            .status(existing.getStatus())
            .build();

        // Créer la transaction bancaire et mettre à jour le solde
        return createTransactionForCheck(existing, cashedDate)
            .flatMap(transaction -> {
                existing.setStatus("CASHED");
                existing.setCashedDate(cashedDate);
                existing.setBankTransactionId(transaction.getId());
                existing.setUpdatedAt(LocalDateTime.now());

                return checkRepositoryPort.save(existing)
                    .flatMap(saved ->
                        // Mettre à jour le solde du compte
                        updateAccountBalance(saved.getBankAccountId(),
                                transaction.getAmount(), transaction.getDirection())
                            .then(auditLogUseCase.log(
                                AuditModule.CHECK,
                                AuditAction.CASH,
                                saved.getId(),
                                saved.getCheckNumber(),
                                original,
                                saved,
                                "Encaissement du chèque émis n°" + saved.getCheckNumber() +
                                " - Transaction " + transaction.getReference() + " créée"
                            ))
                            .thenReturn(saved)
                    );
            })
            .flatMap(this::enrichWithAccountName);
    }

    /**
     * Crée une transaction bancaire pour un chèque encaissé.
     */
    private Mono<BankTransaction> createTransactionForCheck(Check check, LocalDate cashedDate) {
        return findOrCreateCheckTransactionType()
            .flatMap(transactionType ->
                generateCheckReference(check)
                    .flatMap(reference -> {
                        // Déterminer la direction selon le type de chèque
                        // RECEIVED = argent qui entre = CREDIT
                        // ISSUED = argent qui sort = DEBIT
                        String direction = "RECEIVED".equals(check.getCheckType()) ? "CREDIT" : "DEBIT";

                        BankTransaction transaction = BankTransaction.builder()
                            .id(UUID.randomUUID())
                            .bankAccountId(check.getBankAccountId())
                            .transactionTypeId(transactionType.getId())
                            .reference(reference)
                            .transactionDate(cashedDate)
                            .valueDate(cashedDate)
                            .amount(check.getAmount())
                            .direction(direction)
                            .description("Encaissement chèque n°" + check.getCheckNumber() +
                                " - " + check.getPartnerName())
                            .partnerName(check.getPartnerName())
                            .status("VALIDATED")
                            .systemDate(LocalDateTime.now())
                            .isReconciled(false)
                            .build();

                        return bankTransactionRepositoryPort.save(transaction)
                            .doOnSuccess(saved -> LOG.info(
                                "Transaction created for check: txId={}, checkId={}, reference={}",
                                saved.getId(), check.getId(), saved.getReference()))
                            .flatMap(saved -> auditLogUseCase.log(
                                AuditModule.BANK_TRANSACTION,
                                AuditAction.CREATE,
                                saved.getId(),
                                saved.getReference(),
                                null,
                                saved,
                                "Création automatique de transaction pour chèque n°" + check.getCheckNumber()
                            ).thenReturn(saved));
                    })
            );
    }

    /**
     * Trouve le type de transaction pour les chèques, ou en crée un par défaut.
     */
    private Mono<TransactionType> findOrCreateCheckTransactionType() {
        return transactionTypeRepositoryPort.findByCode(CHECK_TYPE_CODE)
            .switchIfEmpty(
                transactionTypeRepositoryPort.findByCategory("CHECK")
                    .next()
                    .switchIfEmpty(
                        Mono.defer(() -> {
                            LOG.warn("No CHECK transaction type found, creating default");
                            TransactionType defaultType = TransactionType.builder()
                                .id(UUID.randomUUID())
                                .code(CHECK_TYPE_CODE)
                                .label("Chèque")
                                .category("CHECK")
                                .description("Transaction par chèque")
                                .isActive(true)
                                .build();
                            return transactionTypeRepositoryPort.save(defaultType);
                        })
                    )
            );
    }

    /**
     * Génère une référence unique pour la transaction de chèque.
     * Format: CHQ-YYYYMM-NNNN
     */
    private Mono<String> generateCheckReference(Check check) {
        String yearMonth = LocalDate.now().format(YEAR_MONTH_FORMATTER);
        String normalizedCode = CHECK_TYPE_CODE.toUpperCase().replace("_", "-");

        return transactionSequenceRepositoryPort.incrementSequence(normalizedCode, yearMonth)
            .then(transactionSequenceRepositoryPort.findLastSequence(normalizedCode, yearMonth))
            .map(sequence -> {
                String formattedSequence = String.format("%04d", sequence);
                return normalizedCode + "-" + yearMonth + "-" + formattedSequence;
            })
            .switchIfEmpty(Mono.error(new BusinessException(
                "Impossible de générer une référence. La séquence n'a pas été trouvée après incrémentation."
            )));
    }

    /**
     * Met à jour le solde du compte bancaire de manière incrémentale.
     */
    private Mono<Void> updateAccountBalance(UUID accountId, BigDecimal amount, String direction) {
        return bankAccountRepositoryPort.findById(accountId)
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

                BigDecimal newBalance = currentBalance.add(adjustment);
                account.setCurrentBalance(newBalance);

                LOG.info("Updating account {} balance: {} + {} = {}",
                    accountId, currentBalance, adjustment, newBalance);
                return bankAccountRepositoryPort.save(account).then();
            });
    }

    @Override
    public Mono<CheckResponse> reject(UUID id, String reason) {
        LOG.info("Rejecting check id={}, reason={}", id, reason);

        return checkRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                String currentStatus = existing.getStatus();
                if ("REJECTED".equals(currentStatus)) {
                    return Mono.error(new BusinessException("Le cheque est deja rejete"));
                }
                if ("CANCELLED".equals(currentStatus)) {
                    return Mono.error(new BusinessException("Impossible de rejeter un cheque annule"));
                }

                if (!"RECEIVED".equals(existing.getCheckType())) {
                    return Mono.error(new BusinessException("Seuls les cheques recus peuvent etre rejetes."));
                }
                if (!"DEPOSITED".equals(existing.getStatus()) && !"IN_PROGRESS".equals(existing.getStatus())) {
                    return Mono.error(new BusinessException(
                        "Un cheque doit etre 'Depose' ou 'En cours' pour pouvoir etre rejete."));
                }

                // Copier l'état précédent pour l'audit
                Check oldState = existing.toBuilder().build();

                existing.setStatus("REJECTED");
                existing.setRejectionReason(reason);
                existing.setUpdatedAt(LocalDateTime.now());

                return checkRepositoryPort.save(existing)
                    .flatMap(saved -> auditLogUseCase.log(
                        AuditModule.CHECK,
                        AuditAction.REJECT,
                        saved.getId(),
                        saved.getCheckNumber(),
                        oldState,
                        saved,
                        "Rejet du chèque n°" + saved.getCheckNumber() + ". Raison: " + reason
                    ).then(enrichWithAccountName(saved)));
            });
    }

    @Override
    public Mono<CheckResponse> cancel(UUID id) {
        LOG.info("Cancelling check id={}", id);

        return checkRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                String currentStatus = existing.getStatus();
                if ("CASHED".equals(currentStatus) || "REJECTED".equals(currentStatus)
                    || "CANCELLED".equals(currentStatus)) {
                    return Mono.error(new BusinessException(
                        "Impossible d'annuler un cheque deja finalise (Encaisse, Rejete ou Annule)."));
                }
                if (!"PENDING".equals(currentStatus) && !"DEPOSITED".equals(currentStatus)) {
                    return Mono.error(new BusinessException(
                        "Le cheque ne peut pas etre annule dans son etat actuel"));
                }

                // Copier l'état précédent pour l'audit
                Check oldState = existing.toBuilder().build();

                existing.setStatus("CANCELLED");
                existing.setUpdatedAt(LocalDateTime.now());

                return checkRepositoryPort.save(existing)
                    .flatMap(saved -> auditLogUseCase.log(
                        AuditModule.CHECK,
                        AuditAction.CANCEL,
                        saved.getId(),
                        saved.getCheckNumber(),
                        oldState,
                        saved,
                        "Annulation du chèque n°" + saved.getCheckNumber()
                    ).then(enrichWithAccountName(saved)));
            });
    }

    @Override
    public Mono<CheckResponse> emit(UUID id, LocalDate emitDate) {
        LOG.info("Emitting check id={} on date={}", id, emitDate);

        return checkRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                if (!"ISSUED".equals(existing.getCheckType())) {
                    return Mono.error(new BusinessException(
                        "Seuls les cheques emis (ISSUED) peuvent etre marques comme emis"));
                }
                if (!"PENDING".equals(existing.getStatus())) {
                    return Mono.error(new BusinessException(
                        "Seuls les cheques en attente peuvent etre emis. Statut actuel: " + existing.getStatus()));
                }

                // Copier l'état précédent pour l'audit
                Check oldState = existing.toBuilder().build();

                existing.setStatus("ISSUED");
                existing.setEmitDate(emitDate);
                existing.setUpdatedAt(LocalDateTime.now());

                return checkRepositoryPort.save(existing)
                    .flatMap(saved -> auditLogUseCase.log(
                        AuditModule.CHECK,
                        AuditAction.UPDATE,
                        saved.getId(),
                        saved.getCheckNumber(),
                        oldState,
                        saved,
                        "Émission du chèque n°" + saved.getCheckNumber() + " au bénéficiaire"
                    ).then(enrichWithAccountName(saved)));
            });
    }

    @Override
    public Mono<CheckResponse> receive(UUID id, LocalDate receiveDate) {
        LOG.info("Receiving check id={} on date={}", id, receiveDate);

        return checkRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                if (!"RECEIVED".equals(existing.getCheckType())) {
                    return Mono.error(new BusinessException(
                        "Seuls les cheques recus (RECEIVED) peuvent etre marques comme recus"));
                }
                if (!"PENDING".equals(existing.getStatus())) {
                    return Mono.error(new BusinessException(
                        "Seuls les cheques en attente peuvent etre marques comme recus"));
                }

                // Copier l'état précédent pour l'audit
                Check oldState = existing.toBuilder().build();

                existing.setStatus("RECEIVED");
                existing.setReceiptDate(receiveDate);
                existing.setUpdatedAt(LocalDateTime.now());

                return checkRepositoryPort.save(existing)
                    .flatMap(saved -> auditLogUseCase.log(
                        AuditModule.CHECK,
                        AuditAction.UPDATE,
                        saved.getId(),
                        saved.getCheckNumber(),
                        oldState,
                        saved,
                        "Réception du chèque n°" + saved.getCheckNumber()
                    ).then(enrichWithAccountName(saved)));
            });
    }

    @Override
    public Mono<CheckResponse> markAsProcessing(UUID id) {
        LOG.info("Marking check id={} as in progress", id);

        return checkRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                if (!"RECEIVED".equals(existing.getCheckType())) {
                    return Mono.error(new BusinessException(
                        "Seuls les cheques recus peuvent etre marques comme en cours de traitement."));
                }

                if (!"DEPOSITED".equals(existing.getStatus())) {
                    return Mono.error(new BusinessException(
                        "Seul un cheque depose peut etre marque comme en cours de traitement."));
                }

                // Copier l'état précédent pour l'audit
                Check oldState = existing.toBuilder().build();

                existing.setStatus("IN_PROGRESS");
                existing.setUpdatedAt(LocalDateTime.now());

                return checkRepositoryPort.save(existing)
                    .flatMap(saved -> auditLogUseCase.log(
                        AuditModule.CHECK,
                        AuditAction.UPDATE,
                        saved.getId(),
                        saved.getCheckNumber(),
                        oldState,
                        saved,
                        "Le suivi du chèque n°" + saved.getCheckNumber() + " a commencé."
                    ).then(enrichWithAccountName(saved)));
            });
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<CheckStatsResponse> getStats() {
        LOG.debug("Calculating check statistics");

        return Mono.zip(
            checkRepositoryPort.countAllExcludingCancelled().defaultIfEmpty(0),
            checkRepositoryPort.sumTotalAmountExcludingCancelled().defaultIfEmpty(BigDecimal.ZERO),
            checkRepositoryPort.countByStatus("PENDING").defaultIfEmpty(0),
            checkRepositoryPort.sumAmountByStatus("PENDING").defaultIfEmpty(BigDecimal.ZERO),
            checkRepositoryPort.countByStatus("ISSUED").defaultIfEmpty(0),
            checkRepositoryPort.sumAmountByStatus("ISSUED").defaultIfEmpty(BigDecimal.ZERO),
            checkRepositoryPort.countByStatus("RECEIVED").defaultIfEmpty(0),
            checkRepositoryPort.sumAmountByStatus("RECEIVED").defaultIfEmpty(BigDecimal.ZERO)
        ).zipWith(Mono.zip(
            checkRepositoryPort.countByStatus("DEPOSITED").defaultIfEmpty(0),
            checkRepositoryPort.sumAmountByStatus("DEPOSITED").defaultIfEmpty(BigDecimal.ZERO),
            checkRepositoryPort.countByStatus("IN_PROGRESS").defaultIfEmpty(0),
            checkRepositoryPort.sumAmountByStatus("IN_PROGRESS").defaultIfEmpty(BigDecimal.ZERO),
            checkRepositoryPort.countByStatus("CASHED").defaultIfEmpty(0),
            checkRepositoryPort.sumAmountByStatus("CASHED").defaultIfEmpty(BigDecimal.ZERO),
            checkRepositoryPort.countByStatus("REJECTED").defaultIfEmpty(0),
            checkRepositoryPort.sumAmountByStatus("REJECTED").defaultIfEmpty(BigDecimal.ZERO)
        )).zipWith(Mono.zip(
            checkRepositoryPort.countOverdueChecks().defaultIfEmpty(0),
            checkRepositoryPort.sumOverdueAmount().defaultIfEmpty(BigDecimal.ZERO),
            checkRepositoryPort.countByCheckType("ISSUED").defaultIfEmpty(0),
            checkRepositoryPort.sumAmountByCheckType("ISSUED").defaultIfEmpty(BigDecimal.ZERO),
            checkRepositoryPort.countByCheckType("RECEIVED").defaultIfEmpty(0),
            checkRepositoryPort.sumAmountByCheckType("RECEIVED").defaultIfEmpty(BigDecimal.ZERO)
        )).map(tuple -> {
            var t1 = tuple.getT1().getT1();
            var t2 = tuple.getT1().getT2();
            var t3 = tuple.getT2();

            return CheckStatsResponse.builder()
                .totalChecks(t1.getT1())
                .totalAmount(t1.getT2())
                .pendingCount(t1.getT3())
                .pendingAmount(t1.getT4())
                .issuedCount(t1.getT5())
                .issuedAmount(t1.getT6())
                .receivedCount(t1.getT7())
                .receivedAmount(t1.getT8())
                .depositedCount(t2.getT1())
                .depositedAmount(t2.getT2())
                .inProgressCount(t2.getT3())
                .inProgressAmount(t2.getT4())
                .cashedCount(t2.getT5())
                .cashedAmount(t2.getT6())
                .rejectedCount(t2.getT7())
                .rejectedAmount(t2.getT8())
                .overdueCount(t3.getT1())
                .overdueAmount(t3.getT2())
                .totalIssuedTypeCount(t3.getT3())
                .totalIssuedTypeAmount(t3.getT4())
                .totalReceivedTypeCount(t3.getT5())
                .totalReceivedTypeAmount(t3.getT6())
                .build();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<CheckResponse> findOverdueChecks() {
        LOG.debug("Finding overdue checks");
        return checkRepositoryPort.findOverdueChecks()
            .flatMap(this::enrichWithAccountName);
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private Mono<CheckResponse> enrichWithAccountName(Check check) {
        Mono<BankAccount> accountMono = bankAccountRepositoryPort.findById(check.getBankAccountId())
            .defaultIfEmpty(BankAccount.builder().name("Compte inconnu").currency("EUR").build());

        Mono<String> checkbookPrefixMono;
        if (check.getCheckbookId() != null) {
            checkbookPrefixMono = checkbookRepositoryPort.findById(check.getCheckbookId())
                .map(Checkbook::getPrefix)
                .defaultIfEmpty("");
        } else {
            checkbookPrefixMono = Mono.just("");
        }

        return Mono.zip(accountMono, checkbookPrefixMono)
            .map(tuple -> checkMapper.toResponseWithDetails(
                check, tuple.getT1().getName(), tuple.getT1().getCurrency(), tuple.getT2()));
    }
}
