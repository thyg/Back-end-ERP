package com.rtcomops.treasury.service;

import com.rtcomops.treasury.dto.request.CreateCheckRequest;
import com.rtcomops.treasury.dto.request.UpdateCheckRequest;
import com.rtcomops.treasury.dto.response.CheckResponse;
import com.rtcomops.treasury.entity.BankAccount;
import com.rtcomops.treasury.entity.BankTransaction;
import com.rtcomops.treasury.entity.Check;
import com.rtcomops.treasury.entity.Checkbook;
import com.rtcomops.treasury.entity.TransactionType;
import com.rtcomops.treasury.enums.AuditAction;
import com.rtcomops.treasury.enums.AuditModule;
import com.rtcomops.treasury.exception.BusinessException;
import com.rtcomops.treasury.exception.DuplicateResourceException;
import com.rtcomops.treasury.exception.ResourceNotFoundException;
import com.rtcomops.treasury.mapper.CheckMapper;
import com.rtcomops.treasury.repository.BankAccountRepository;
import com.rtcomops.treasury.repository.BankTransactionRepository;
import com.rtcomops.treasury.repository.CheckRepository;
import com.rtcomops.treasury.repository.CheckbookRepository;
import com.rtcomops.treasury.repository.TransactionSequenceRepository;
import com.rtcomops.treasury.repository.TransactionTypeRepository;
import com.rtcomops.treasury.util.NumberToWordsConverter;
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
 * Service layer for Check operations.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Service
@Transactional
public class CheckService {

    private static final Logger LOG = LoggerFactory.getLogger(CheckService.class);
    private static final String RESOURCE_NAME = "Check";
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final String CHECK_TYPE_CODE = "CHQ";

    private final CheckRepository checkRepository;
    private final BankAccountRepository accountRepository;
    private final CheckbookRepository checkbookRepository;
    private final BankTransactionRepository transactionRepository;
    private final TransactionTypeRepository transactionTypeRepository;
    private final TransactionSequenceRepository sequenceRepository;
    private final CheckMapper checkMapper;
    private final AuditLogService auditLogService;
    private final BalanceValidationService balanceValidationService;

    public CheckService(
            CheckRepository checkRepository,
            BankAccountRepository accountRepository,
            CheckbookRepository checkbookRepository,
            BankTransactionRepository transactionRepository,
            TransactionTypeRepository transactionTypeRepository,
            TransactionSequenceRepository sequenceRepository,
            CheckMapper checkMapper,
            AuditLogService auditLogService,
            BalanceValidationService balanceValidationService) {
        this.checkRepository = checkRepository;
        this.accountRepository = accountRepository;
        this.checkbookRepository = checkbookRepository;
        this.transactionRepository = transactionRepository;
        this.transactionTypeRepository = transactionTypeRepository;
        this.sequenceRepository = sequenceRepository;
        this.checkMapper = checkMapper;
        this.auditLogService = auditLogService;
        this.balanceValidationService = balanceValidationService;
    }

    @Transactional(readOnly = true)
    public Flux<CheckResponse> findAll(@Nullable UUID checkbookId) {
        Flux<Check> checks;
        if (checkbookId != null) {
            LOG.debug("Finding all checks for checkbookId={}", checkbookId);
            checks = checkRepository.findByCheckbookId(checkbookId);
        } else {
            LOG.debug("Finding all checks");
            checks = checkRepository.findAllOrderByDateDesc();
        }
        return checks.flatMap(this::enrichWithAccountName);
    }

    @Transactional(readOnly = true)
    public Mono<CheckResponse> findById(UUID id) {
        LOG.debug("Finding check by id={}", id);
        return checkRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(this::enrichWithAccountName);
    }

    @Transactional(readOnly = true)
    public Flux<CheckResponse> findByType(String checkType) {
        LOG.debug("Finding checks by type={}", checkType);
        return checkRepository.findByCheckType(checkType.toUpperCase())
            .flatMap(this::enrichWithAccountName);
    }

    @Transactional(readOnly = true)
    public Flux<CheckResponse> findByStatus(String status) {
        LOG.debug("Finding checks by status={}", status);
        return checkRepository.findByStatus(status.toUpperCase())
            .flatMap(this::enrichWithAccountName);
    }

    @Transactional(readOnly = true)
    public Flux<CheckResponse> findByTypeAndStatus(String checkType, String status) {
        LOG.debug("Finding checks by type={} and status={}", checkType, status);
        return checkRepository.findByCheckTypeAndStatus(checkType.toUpperCase(), status.toUpperCase())
            .flatMap(this::enrichWithAccountName);
    }

    @Transactional(readOnly = true)
    public Flux<CheckResponse> findByAccountId(UUID accountId) {
        LOG.debug("Finding checks by accountId={}", accountId);
        return checkRepository.findByBankAccountId(accountId)
            .flatMap(this::enrichWithAccountName);
    }

    @Transactional(readOnly = true)
    public Flux<CheckResponse> findPendingChecksDueBefore(LocalDate date) {
        LOG.debug("Finding pending checks due before {}", date);
        return checkRepository.findPendingChecksDueBefore(date)
            .flatMap(this::enrichWithAccountName);
    }

    public Mono<CheckResponse> create(CreateCheckRequest request) {
        LOG.info("Creating check for account={}", request.getBankAccountId());

        return accountRepository.findById(request.getBankAccountId())
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankAccount", request.getBankAccountId())))
            .flatMap(account -> {
                // Verification du solde pour les cheques EMIS uniquement
                // Utilise le BalanceValidationService pour prendre en compte le decouvert
                if ("ISSUED".equalsIgnoreCase(request.getCheckType())) {
                    return balanceValidationService.validateDebitOperation(
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

                // For RECEIVED checks, always use manual creation since the check number
                // is provided by the third party. The checkbookId (if provided) will be used
                // for association only, not for auto-generating the check number.
                if ("RECEIVED".equalsIgnoreCase(request.getCheckType())) {
                    return createCheckManual(request, account);
                }

                // For ISSUED checks: if checkbookId is provided, auto-generate the number
                if (request.getCheckbookId() != null) {
                    return createCheckFromCheckbook(request, account);
                } else {
                    // Manual check number - validation faite dans createCheckManual
                    return createCheckManual(request, account);
                }
            });
    }

    /**
     * Creates a check with number auto-generated from a checkbook.
     */
    private Mono<CheckResponse> createCheckFromCheckbook(CreateCheckRequest request, BankAccount account) {
        return checkbookRepository.findById(request.getCheckbookId())
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Checkbook", request.getCheckbookId())))
            .flatMap(checkbook -> {
                // Validate checkbook belongs to the same account (only for real checkbooks)
                // System/fictitious checkbooks (FICTIF type) can be used with any account
                if (!checkbook.isFictif() && checkbook.getBankAccountId() != null
                    && !checkbook.getBankAccountId().equals(request.getBankAccountId())) {
                    return Mono.error(new BusinessException(
                        "Le chéquier n'appartient pas au compte bancaire spécifié"));
                }

                // Check if checkbook has available checks
                if (!checkbook.hasAvailableChecks()) {
                    return Mono.error(new BusinessException(
                        "Le chéquier n'a plus de chèques disponibles"));
                }

                // Get the next check number
                String checkNumber = checkbook.getNextCheckNumber();

                // Increment the checkbook counter
                return checkbookRepository.incrementCurrentNumber(checkbook.getId())
                    .flatMap(rowsAffected -> {
                        if (rowsAffected == 0) {
                            return Mono.error(new BusinessException(
                                "Erreur lors de l'allocation du numéro de chèque"));
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
     * For RECEIVED checks without a checkbookId, automatically assigns the system (fictitious) checkbook.
     *
     * Note: The system checkbook is auto-created by CheckbookInitializer on application startup.
     * If it doesn't exist, this method will still create the check but without checkbook assignment.
     */
    private Mono<CheckResponse> createCheckManual(CreateCheckRequest request, BankAccount account) {
        // Vérifier que le numéro de chèque est fourni
        if (request.getCheckNumber() == null || request.getCheckNumber().isBlank()) {
            return Mono.error(new BusinessException(
                "Le numéro de chèque est obligatoire pour une saisie manuelle"));
        }

        return checkRepository.existsByCheckNumberAndAccountAndType(
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
                    // If checkbookId is provided in the request (e.g., from frontend), use it directly
                    if (request.getCheckbookId() != null) {
                        return checkbookRepository.findById(request.getCheckbookId())
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

                    // Otherwise, auto-assign the system checkbook
                    return checkbookRepository.findByIsSystemTrue()
                        .flatMap(systemCheckbook -> {
                            // Vérifier que le chéquier système est actif
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
                            // Le chéquier système n'existe pas - on continue sans erreur
                            // mais on log un avertissement car il devrait avoir été créé par CheckbookInitializer
                            LOG.warn("System checkbook not found. It should be created by CheckbookInitializer on startup. " +
                                "Creating received check {} without checkbook assignment.", entity.getCheckNumber());
                            return saveCheck(entity, account.getName(), account.getCurrency(), null);
                        }))
                        .onErrorResume(e -> {
                            // En cas d'erreur lors de la recherche du chéquier système,
                            // on continue la création du chèque sans l'assigner
                            LOG.error("Error while fetching system checkbook, proceeding without assignment: {}", e.getMessage());
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
        return checkRepository.save(entity)
            .flatMap(saved -> auditLogService.log(
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

    public Mono<CheckResponse> update(UUID id, UpdateCheckRequest request) {
        LOG.info("Updating check id={}", id);
        
        return checkRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                Mono<Void> validation = Mono.empty();
                
                if (request.getCheckNumber() != null && 
                    !request.getCheckNumber().equals(existing.getCheckNumber())) {
                    validation = checkRepository.existsByCheckNumberAndAccountAndTypeAndIdNot(
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
                    return checkRepository.save(updated)
                        .flatMap(saved ->
                            auditLogService.log(
                                AuditModule.CHECK,
                                AuditAction.UPDATE,
                                saved.getId(),
                                saved.getCheckNumber(),
                                existing,
                                saved,
                                "Mise à jour du chèque n°" + saved.getCheckNumber()
                            ).then(Mono.just(saved))
                        )
                        .cast(Check.class)
                        .flatMap(this::enrichWithAccountName);
                }));
            });
    }

    /**
     * Deletes a check (only PENDING checks can be deleted).
     *
     * IMPORTANT - Business Rule: Deleting a check does NOT decrement the checkbook's currentNumber.
     * This is intentional for accounting integrity:
     * - Once a check number is allocated from a checkbook, it is considered "consumed"
     * - Even if the check is deleted before being emitted, the number should not be reused
     * - This prevents potential gaps in audit trails and accounting records
     * - In real-world scenarios, deleted checks would typically be voided/cancelled rather than reused
     *
     * @param id the check ID to delete
     * @return Mono<Void>
     */
    public Mono<Void> delete(UUID id) {
        LOG.info("Deleting check id={}", id);

        return checkRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(check -> {
                // Étape 1: Valider le statut
                if (!"PENDING".equals(check.getStatus())) {
                    return Mono.error(new BusinessException("Seul un chèque au statut 'En attente' peut être supprimé."));
                }

                // Note: We intentionally do NOT decrement the checkbook's currentNumber here.
                // See method Javadoc for the business rationale.

                // Étape 2: Enchaîner les opérations de manière séquentielle
                return auditLogService.log(
                        AuditModule.CHECK,
                        AuditAction.DELETE,
                        check.getId(),
                        check.getCheckNumber(),
                        check,
                        null,
                        "Suppression du chèque n°" + check.getCheckNumber()
                    )
                    .then(checkRepository.delete(check)) // Étape 3: Après le log, supprimer le chèque
                    .doOnSuccess(v -> LOG.info("Check deleted successfully: id={}", id)); // Étape 4: En cas de succès, logger l'information
            });
    }

    public Mono<CheckResponse> deposit(UUID id, LocalDate depositDate) {
        LOG.info("Depositing check id={} on date={}", id, depositDate);

        return checkRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                // Validation: seuls les chèques reçus peuvent être déposés
                if (!"RECEIVED".equals(existing.getCheckType())) {
                    return Mono.error(new BusinessException(
                        "Seuls les chèques reçus peuvent être remis en banque"));
                }

                // Validation: seuls les chèques PENDING ou RECEIVED peuvent être déposés
                if (!"PENDING".equals(existing.getStatus()) && !"RECEIVED".equals(existing.getStatus())) {
                    return Mono.error(new BusinessException(
                        "Seuls les chèques en attente ou reçus peuvent être remis en banque"));
                }

                Check original = existing.toBuilder().build();
                existing.setStatus("DEPOSITED");
                existing.setDepositDate(depositDate);
                existing.setNew(false);

                return checkRepository.save(existing)
                    .flatMap(saved -> auditLogService.log(
                            AuditModule.CHECK,
                            AuditAction.DEPOSIT,
                            saved.getId(),
                            saved.getCheckNumber(),
                            original,
                            saved,
                            "Remise en banque du chèque n°" + saved.getCheckNumber()
                    ).thenReturn(saved))
                    .flatMap(this::enrichWithAccountName);
            });
    }

    public Mono<CheckResponse> cash(UUID id, LocalDate cashedDate) {
        LOG.info("Cashing check id={} on date={}", id, cashedDate);

        return checkRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                // Validation des transitions de statut
                if ("RECEIVED".equals(existing.getCheckType())) {
    if (!"DEPOSITED".equals(existing.getStatus()) && !"IN_PROGRESS".equals(existing.getStatus())) {
        return Mono.error(new BusinessException(
            "Un chèque reçu doit être 'Déposé' avant de pouvoir être encaissé."));
    }
} else { // Chèque ÉMIS
    if (!"ISSUED".equals(existing.getStatus())) {
        return Mono.error(new BusinessException(
            "Un chèque émis doit être au statut 'Émis' pour être marqué comme payé."));
    }
}

                Check original = existing.toBuilder().build();

                // Créer la transaction bancaire et mettre à jour le solde
                return createTransactionForCheck(existing, cashedDate)
                    .flatMap(transaction -> {
                        existing.setStatus("CASHED");
                        existing.setCashedDate(cashedDate);
                        existing.setBankTransactionId(transaction.getId());
                        existing.setNew(false);

                        return checkRepository.save(existing)
                            .flatMap(saved ->
                                // Mettre à jour le solde du compte
                                updateAccountBalance(saved.getBankAccountId(),
                                        transaction.getAmount(), transaction.getDirection())
                                    .then(auditLogService.log(
                                        AuditModule.CHECK,
                                        AuditAction.CASH,
                                        saved.getId(),
                                        saved.getCheckNumber(),
                                        original,
                                        saved,
                                        "Encaissement du chèque n°" + saved.getCheckNumber() +
                                        " - Transaction " + transaction.getReference() + " créée"
                                    ))
                                    .thenReturn(saved)
                            );
                    })
                    .flatMap(this::enrichWithAccountName);
            });
    }

    /**
     * Crée une transaction bancaire pour un chèque encaissé.
     *
     * @param check le chèque encaissé
     * @param cashedDate la date d'encaissement
     * @return Mono de la transaction créée
     */
    private Mono<BankTransaction> createTransactionForCheck(Check check, LocalDate cashedDate) {
        // Trouver ou créer le type de transaction pour les chèques
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
                            .status("VALIDATED") // Transaction validée automatiquement
                            .systemDate(LocalDateTime.now())
                            .isReconciled(false)
                            .isNew(true)
                            .build();

                        return transactionRepository.save(transaction)
                            .doOnSuccess(saved -> LOG.info(
                                "Transaction created for check: txId={}, checkId={}, reference={}",
                                saved.getId(), check.getId(), saved.getReference()))
                            .flatMap(saved -> auditLogService.log(
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
        return transactionTypeRepository.findByCode(CHECK_TYPE_CODE)
            .switchIfEmpty(
                // Chercher dans la catégorie CHECK
                transactionTypeRepository.findByCategory("CHECK")
                    .next()
                    .switchIfEmpty(
                        // Créer un type par défaut si aucun n'existe
                        Mono.defer(() -> {
                            LOG.warn("No CHECK transaction type found, creating default");
                            TransactionType defaultType = TransactionType.builder()
                                .id(UUID.randomUUID())
                                .code(CHECK_TYPE_CODE)
                                .label("Chèque")
                                .category("CHECK")
                                .description("Transaction par chèque")
                                .isActive(true)
                                .isNew(true)
                                .build();
                            return transactionTypeRepository.save(defaultType);
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
        
        // --- CORRECTION ICI ---
        // On utilise la constante de la classe, CHECK_TYPE_CODE, au lieu de la variable inexistante 'typeCode'.
        String normalizedCode = CHECK_TYPE_CODE.toUpperCase().replace("_", "-");

        // On incrémente d'abord, PUIS on lit la valeur.
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
     * Met à jour le solde du compte bancaire de manière incrémentale.
     *
     * @param accountId l'ID du compte bancaire
     * @param amount le montant de la transaction
     * @param direction le sens de la transaction (CREDIT ou DEBIT)
     * @return Mono<Void>
     */
    private Mono<Void> updateAccountBalance(UUID accountId, BigDecimal amount, String direction) {
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

                BigDecimal newBalance = currentBalance.add(adjustment);
                account.setCurrentBalance(newBalance);
                account.setNew(false);

                LOG.info("Updating account {} balance: {} + {} = {}",
                    accountId, currentBalance, adjustment, newBalance);
                return accountRepository.save(account).then();
            });
    }

    public Mono<CheckResponse> reject(UUID id, String reason) {
        LOG.info("Rejecting check id={}, reason={}", id, reason);

        return checkRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                // Validation des transitions de statut
                String currentStatus = existing.getStatus();
                if ("REJECTED".equals(currentStatus)) {
                    return Mono.error(new BusinessException("Le chèque est déjà rejeté"));
                }
                if ("CANCELLED".equals(currentStatus)) {
                    return Mono.error(new BusinessException("Impossible de rejeter un chèque annulé"));
                }
                   
                if (!"RECEIVED".equals(existing.getCheckType())) {
    return Mono.error(new BusinessException("Seuls les chèques reçus peuvent être rejetés."));
}
if (!"DEPOSITED".equals(existing.getStatus()) && !"IN_PROGRESS".equals(existing.getStatus())) {
    return Mono.error(new BusinessException(
        "Un chèque doit être 'Déposé' ou 'En cours' pour pouvoir être rejeté."));
}


                
                Check original = existing.toBuilder().build();
                existing.setStatus("REJECTED");
                existing.setRejectionReason(reason);
                existing.setNew(false);
                return checkRepository.save(existing)
                    .flatMap(saved -> auditLogService.log(
                            AuditModule.CHECK,
                            AuditAction.REJECT,
                            saved.getId(),
                            saved.getCheckNumber(),
                            original,
                            saved,
                            "Rejet du chèque n°" + saved.getCheckNumber() + ". Raison: " + reason
                    ).thenReturn(saved))
                    .flatMap(this::enrichWithAccountName);
            });
    }

    public Mono<CheckResponse> cancel(UUID id) {
        LOG.info("Cancelling check id={}", id);

        return checkRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                // Validation des transitions de statut
                String currentStatus = existing.getStatus();
               if ("CASHED".equals(currentStatus) || "REJECTED".equals(currentStatus) || "CANCELLED".equals(currentStatus)) {
    return Mono.error(new BusinessException(
        "Impossible d'annuler un chèque déjà finalisé (Encaissé, Rejeté ou Annulé)."));
}
                // Seuls les chèques PENDING ou DEPOSITED peuvent être annulés
                if (!"PENDING".equals(currentStatus) && !"DEPOSITED".equals(currentStatus)) {
                    return Mono.error(new BusinessException(
                        "Le chèque ne peut pas être annulé dans son état actuel"));
                }

                Check original = existing.toBuilder().build();
                existing.setStatus("CANCELLED");
                existing.setNew(false);
                return checkRepository.save(existing)
                    .flatMap(saved -> auditLogService.log(
                            AuditModule.CHECK,
                            AuditAction.CANCEL,
                            saved.getId(),
                            saved.getCheckNumber(),
                            original,
                            saved,
                            "Annulation du chèque n°" + saved.getCheckNumber()
                    ).thenReturn(saved))
                    .flatMap(this::enrichWithAccountName);
            });
    }

    /**
     * Emits a check (hands it to the beneficiary).
     * Transition: PENDING → ISSUED
     * Only applicable to ISSUED type checks.
     */
    public Mono<CheckResponse> emit(UUID id, LocalDate emitDate) {
        LOG.info("Emitting check id={} on date={}", id, emitDate);

        return checkRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                if (!"ISSUED".equals(existing.getCheckType())) {
                    return Mono.error(new BusinessException(
                        "Seuls les chèques émis (ISSUED) peuvent être marqués comme émis"));
                }
                if (!"PENDING".equals(existing.getStatus())) {
                    return Mono.error(new BusinessException(
                        "Seuls les chèques en attente peuvent être émis. Statut actuel: " + existing.getStatus()));
                }

                Check original = existing.toBuilder().build();
                existing.setStatus("ISSUED");
                existing.setEmitDate(emitDate);
                existing.setNew(false);

                return checkRepository.save(existing)
                    .flatMap(saved -> auditLogService.log(
                            AuditModule.CHECK,
                            AuditAction.UPDATE,
                            saved.getId(),
                            saved.getCheckNumber(),
                            original,
                            saved,
                            "Émission du chèque n°" + saved.getCheckNumber() + " au bénéficiaire"
                    ).thenReturn(saved))
                    .flatMap(this::enrichWithAccountName);
            });
    }

    /**
     * Marks a received check as "in our possession".
     * Transition: PENDING → RECEIVED
     * Only applicable to RECEIVED type checks.
     */
    public Mono<CheckResponse> receive(UUID id, LocalDate receiveDate) {
        LOG.info("Receiving check id={} on date={}", id, receiveDate);

        return checkRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                if (!"RECEIVED".equals(existing.getCheckType())) {
                    return Mono.error(new BusinessException(
                        "Seuls les chèques reçus (RECEIVED) peuvent être marqués comme reçus"));
                }
                if (!"PENDING".equals(existing.getStatus())) {
                    return Mono.error(new BusinessException(
                        "Seuls les chèques en attente peuvent être marqués comme reçus"));
                }

                Check original = existing.toBuilder().build();
                existing.setStatus("RECEIVED");
                existing.setReceiptDate(receiveDate);
                existing.setNew(false);

                return checkRepository.save(existing)
                    .flatMap(saved -> auditLogService.log(
                            AuditModule.CHECK,
                            AuditAction.UPDATE,
                            saved.getId(),
                            saved.getCheckNumber(),
                            original,
                            saved,
                            "Réception du chèque n°" + saved.getCheckNumber()
                    ).thenReturn(saved))
                    .flatMap(this::enrichWithAccountName);
            });
    }

    /**
     * Marks a deposited check as "in progress" (being processed by the bank).
     * Transition: DEPOSITED → IN_PROGRESS
     * Only applicable to RECEIVED type checks.
     *
     * @param id the check ID
     * @return Mono of the updated CheckResponse
     */
    public Mono<CheckResponse> markAsProcessing(UUID id) {
        LOG.info("Marking check id={} as in progress", id);

        return checkRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(existing -> {
                // Validation: seuls les chèques RECEIVED peuvent passer en IN_PROGRESS
                if (!"RECEIVED".equals(existing.getCheckType())) {
                    return Mono.error(new BusinessException(
                        "Seuls les chèques reçus peuvent être marqués comme en cours de traitement."));
                }

                // Validation: seuls les chèques DEPOSITED peuvent passer en IN_PROGRESS
                if (!"DEPOSITED".equals(existing.getStatus())) {
                    return Mono.error(new BusinessException(
                        "Seul un chèque déposé peut être marqué comme en cours de traitement."));
                }

                Check original = existing.toBuilder().build();
                existing.setStatus("IN_PROGRESS");
                existing.setNew(false);

                return checkRepository.save(existing)
                    .flatMap(saved -> auditLogService.log(
                            AuditModule.CHECK,
                            AuditAction.UPDATE,
                            saved.getId(),
                            saved.getCheckNumber(),
                            original,
                            saved,
                            "Le suivi du chèque n°" + saved.getCheckNumber() + " a commencé."
                    ).thenReturn(saved))
                    .flatMap(this::enrichWithAccountName);
            });
    }

    // =========================================================================
    // STATISTIQUES
    // =========================================================================

    /**
     * Retrieves aggregated statistics for all checks.
     * Provides KPIs for the checks dashboard.
     *
     * @return Mono of CheckStatsResponse containing all statistics
     */
    @Transactional(readOnly = true)
    public Mono<com.rtcomops.treasury.dto.response.CheckStatsResponse> getStats() {
        LOG.debug("Calculating check statistics");

        return Mono.zip(
            // Totaux
            checkRepository.countAllExcludingCancelled().defaultIfEmpty(0),
            checkRepository.sumTotalAmountExcludingCancelled().defaultIfEmpty(BigDecimal.ZERO),
            // PENDING
            checkRepository.countByStatus("PENDING").defaultIfEmpty(0),
            checkRepository.sumAmountByStatus("PENDING").defaultIfEmpty(BigDecimal.ZERO),
            // ISSUED (chèques émis en circulation)
            checkRepository.countByStatus("ISSUED").defaultIfEmpty(0),
            checkRepository.sumAmountByStatus("ISSUED").defaultIfEmpty(BigDecimal.ZERO),
            // RECEIVED (chèques reçus en main)
            checkRepository.countByStatus("RECEIVED").defaultIfEmpty(0),
            checkRepository.sumAmountByStatus("RECEIVED").defaultIfEmpty(BigDecimal.ZERO)
        ).zipWith(Mono.zip(
            // DEPOSITED
            checkRepository.countByStatus("DEPOSITED").defaultIfEmpty(0),
            checkRepository.sumAmountByStatus("DEPOSITED").defaultIfEmpty(BigDecimal.ZERO),
            // IN_PROGRESS
            checkRepository.countByStatus("IN_PROGRESS").defaultIfEmpty(0),
            checkRepository.sumAmountByStatus("IN_PROGRESS").defaultIfEmpty(BigDecimal.ZERO),
            // CASHED
            checkRepository.countByStatus("CASHED").defaultIfEmpty(0),
            checkRepository.sumAmountByStatus("CASHED").defaultIfEmpty(BigDecimal.ZERO),
            // REJECTED
            checkRepository.countByStatus("REJECTED").defaultIfEmpty(0),
            checkRepository.sumAmountByStatus("REJECTED").defaultIfEmpty(BigDecimal.ZERO)
        )).zipWith(Mono.zip(
            // Overdue checks
            checkRepository.countOverdueChecks().defaultIfEmpty(0),
            checkRepository.sumOverdueAmount().defaultIfEmpty(BigDecimal.ZERO),
            // By type: ISSUED
            checkRepository.countByCheckType("ISSUED").defaultIfEmpty(0),
            checkRepository.sumAmountByCheckType("ISSUED").defaultIfEmpty(BigDecimal.ZERO),
            // By type: RECEIVED
            checkRepository.countByCheckType("RECEIVED").defaultIfEmpty(0),
            checkRepository.sumAmountByCheckType("RECEIVED").defaultIfEmpty(BigDecimal.ZERO)
        )).map(tuple -> {
            var t1 = tuple.getT1().getT1(); // First group of 8
            var t2 = tuple.getT1().getT2(); // Second group of 8
            var t3 = tuple.getT2();          // Third group of 6

            return com.rtcomops.treasury.dto.response.CheckStatsResponse.builder()
                // Totaux
                .totalChecks(t1.getT1())
                .totalAmount(t1.getT2())
                // PENDING
                .pendingCount(t1.getT3())
                .pendingAmount(t1.getT4())
                // ISSUED
                .issuedCount(t1.getT5())
                .issuedAmount(t1.getT6())
                // RECEIVED
                .receivedCount(t1.getT7())
                .receivedAmount(t1.getT8())
                // DEPOSITED
                .depositedCount(t2.getT1())
                .depositedAmount(t2.getT2())
                // IN_PROGRESS
                .inProgressCount(t2.getT3())
                .inProgressAmount(t2.getT4())
                // CASHED
                .cashedCount(t2.getT5())
                .cashedAmount(t2.getT6())
                // REJECTED
                .rejectedCount(t2.getT7())
                .rejectedAmount(t2.getT8())
                // Overdue
                .overdueCount(t3.getT1())
                .overdueAmount(t3.getT2())
                // By type
                .totalIssuedTypeCount(t3.getT3())
                .totalIssuedTypeAmount(t3.getT4())
                .totalReceivedTypeCount(t3.getT5())
                .totalReceivedTypeAmount(t3.getT6())
                .build();
        });
    }

    /**
     * Finds all checks with overdue due dates.
     *
     * @return Flux of overdue checks
     */
    @Transactional(readOnly = true)
    public Flux<CheckResponse> findOverdueChecks() {
        LOG.debug("Finding overdue checks");
        return checkRepository.findOverdueChecks()
            .flatMap(this::enrichWithAccountName);
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private Mono<CheckResponse> enrichWithAccountName(Check check) {
        Mono<BankAccount> accountMono = accountRepository.findById(check.getBankAccountId())
            .defaultIfEmpty(BankAccount.builder().name("Compte inconnu").currency("EUR").build());

        Mono<String> checkbookPrefixMono;
        if (check.getCheckbookId() != null) {
            checkbookPrefixMono = checkbookRepository.findById(check.getCheckbookId())
                .map(Checkbook::getPrefix)
                .defaultIfEmpty("");
        } else {
            checkbookPrefixMono = Mono.just("");
        }

        return Mono.zip(accountMono, checkbookPrefixMono)
            .map(tuple -> checkMapper.toResponseWithDetails(check, tuple.getT1().getName(), tuple.getT1().getCurrency(), tuple.getT2()));
    }
}
