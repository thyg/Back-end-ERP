package com.rtcomops.treasury.service;

import com.rtcomops.treasury.dto.request.CreateCheckDepositRequest;
import com.rtcomops.treasury.dto.response.CheckDepositResponse;
import com.rtcomops.treasury.dto.response.CheckResponse;
import com.rtcomops.treasury.entity.BankAccount;
import com.rtcomops.treasury.entity.BankTransaction;
import com.rtcomops.treasury.entity.Check;
import com.rtcomops.treasury.entity.CheckDeposit;
import com.rtcomops.treasury.entity.TransactionType;
import com.rtcomops.treasury.enums.AuditAction;
import com.rtcomops.treasury.enums.AuditModule;
import com.rtcomops.treasury.exception.BusinessException;
import com.rtcomops.treasury.exception.ResourceNotFoundException;
import com.rtcomops.treasury.mapper.CheckDepositMapper;
import com.rtcomops.treasury.mapper.CheckMapper;
import com.rtcomops.treasury.repository.BankAccountRepository;
import com.rtcomops.treasury.repository.BankTransactionRepository;
import com.rtcomops.treasury.repository.CheckDepositRepository;
import com.rtcomops.treasury.repository.CheckRepository;
import com.rtcomops.treasury.repository.TransactionSequenceRepository;
import com.rtcomops.treasury.repository.TransactionTypeRepository;
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
import java.util.List;
import java.util.UUID;

/**
 * Service layer for CheckDeposit operations.
 *
 * <p>Manages batch check deposits (remises de cheques en lot) with a 3-step workflow:</p>
 * <ol>
 *   <li><strong>Create (PENDING)</strong>: Checks are assigned to the deposit but remain RECEIVED</li>
 *   <li><strong>Confirm Deposit (DEPOSITED)</strong>: Checks are physically deposited at the bank</li>
 *   <li><strong>Cash (CASHED)</strong>: Funds are received, bank transaction created</li>
 * </ol>
 *
 * @author RT-ComOps Team
 * @version 2.0.0
 * @since 2026-02-16
 */
@Service
@Transactional
public class CheckDepositService {

    private static final Logger LOG = LoggerFactory.getLogger(CheckDepositService.class);
    private static final String RESOURCE_NAME = "CheckDeposit";
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final String DEPOSIT_TYPE_CODE = "REM";
    private static final String CHECK_TYPE_CODE = "CHQ";

    // Workflow statuses
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_DEPOSITED = "DEPOSITED";
    private static final String STATUS_CASHED = "CASHED";

    private final CheckDepositRepository depositRepository;
    private final CheckRepository checkRepository;
    private final BankAccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;
    private final TransactionTypeRepository transactionTypeRepository;
    private final TransactionSequenceRepository sequenceRepository;
    private final CheckDepositMapper depositMapper;
    private final CheckMapper checkMapper;
    private final AuditLogService auditLogService;

    public CheckDepositService(
            CheckDepositRepository depositRepository,
            CheckRepository checkRepository,
            BankAccountRepository accountRepository,
            BankTransactionRepository transactionRepository,
            TransactionTypeRepository transactionTypeRepository,
            TransactionSequenceRepository sequenceRepository,
            CheckDepositMapper depositMapper,
            CheckMapper checkMapper,
            AuditLogService auditLogService) {
        this.depositRepository = depositRepository;
        this.checkRepository = checkRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionTypeRepository = transactionTypeRepository;
        this.sequenceRepository = sequenceRepository;
        this.depositMapper = depositMapper;
        this.checkMapper = checkMapper;
        this.auditLogService = auditLogService;
    }

    // =========================================================================
    // QUERY METHODS
    // =========================================================================

    /**
     * Retrieves all check deposits.
     *
     * @return Flux of CheckDepositResponse
     */
    @Transactional(readOnly = true)
    public Flux<CheckDepositResponse> findAll() {
        LOG.debug("Finding all check deposits");
        return depositRepository.findAllOrderByDateDesc()
            .flatMap(this::enrichWithAccountDetails);
    }

    /**
     * Retrieves all check deposits for a specific bank account.
     *
     * @param bankAccountId the bank account ID
     * @return Flux of CheckDepositResponse
     */
    @Transactional(readOnly = true)
    public Flux<CheckDepositResponse> findByBankAccountId(UUID bankAccountId) {
        LOG.debug("Finding check deposits for account={}", bankAccountId);
        return depositRepository.findByBankAccountId(bankAccountId)
            .flatMap(this::enrichWithAccountDetails);
    }

    /**
     * Retrieves a check deposit by its ID with full details including checks.
     *
     * @param id the deposit ID
     * @return Mono of CheckDepositResponse
     */
    @Transactional(readOnly = true)
    public Mono<CheckDepositResponse> findById(UUID id) {
        LOG.debug("Finding check deposit by id={}", id);
        return depositRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(this::enrichWithFullDetails);
    }

    /**
     * Retrieves all unreconciled check deposits (PENDING or DEPOSITED status).
     *
     * @return Flux of CheckDepositResponse
     */
    @Transactional(readOnly = true)
    public Flux<CheckDepositResponse> findUnreconciled() {
        LOG.debug("Finding unreconciled check deposits");
        return depositRepository.findUnreconciled()
            .flatMap(this::enrichWithAccountDetails);
    }

    // =========================================================================
    // STEP 1: CREATE DEPOSIT (PENDING)
    // =========================================================================

    /**
     * Creates a new check deposit batch with status PENDING.
     *
     * <p>Workflow Step 1: The deposit is created and checks are assigned to it,
     * but their status remains RECEIVED. They become unavailable for other deposits.</p>
     *
     * <p>Validations performed:</p>
     * <ul>
     *   <li>All checks must exist</li>
     *   <li>All checks must have status RECEIVED</li>
     *   <li>All checks must not already be assigned to another deposit</li>
     *   <li>All checks must belong to the same bankAccountId as the request</li>
     * </ul>
     *
     * @param request the create request
     * @return Mono of CheckDepositResponse
     */
    public Mono<CheckDepositResponse> createDeposit(CreateCheckDepositRequest request) {
        LOG.info("Creating check deposit for account={} with {} checks",
            request.getBankAccountId(), request.getCheckIds().size());

        // Validate bank account exists
        return accountRepository.findById(request.getBankAccountId())
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankAccount", request.getBankAccountId())))
            .flatMap(account -> validateAndCreateDeposit(request, account));
    }

    /**
     * Validates the request and creates the deposit with PENDING status.
     */
    private Mono<CheckDepositResponse> validateAndCreateDeposit(
            CreateCheckDepositRequest request, BankAccount account) {

        List<UUID> checkIds = request.getCheckIds();

        return checkRepository.findAllById(checkIds)
            .collectList()
            .flatMap(checks -> {
                // Validation 1: All checks must exist
                if (checks.size() != checkIds.size()) {
                    return Mono.error(new BusinessException(
                        String.format("Certains cheques n'ont pas ete trouves. Attendu: %d, Trouve: %d",
                            checkIds.size(), checks.size())));
                }

                // Validation 2: All checks must have status RECEIVED
                List<Check> invalidStatusChecks = checks.stream()
                    .filter(c -> !"RECEIVED".equals(c.getStatus()))
                    .toList();
                if (!invalidStatusChecks.isEmpty()) {
                    String invalidNumbers = invalidStatusChecks.stream()
                        .map(c -> c.getCheckNumber() + " (" + c.getStatus() + ")")
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");
                    return Mono.error(new BusinessException(
                        "Tous les cheques doivent avoir le statut 'RECEIVED'. Cheques invalides: " + invalidNumbers));
                }

                // Validation 3: All checks must not already be assigned to another deposit
                List<Check> alreadyAssignedChecks = checks.stream()
                    .filter(c -> c.getCheckDepositId() != null)
                    .toList();
                if (!alreadyAssignedChecks.isEmpty()) {
                    String assignedNumbers = alreadyAssignedChecks.stream()
                        .map(Check::getCheckNumber)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");
                    return Mono.error(new BusinessException(
                        "Certains cheques sont deja assignes a une autre remise: " + assignedNumbers));
                }

                // Validation 4: All checks must belong to the same bank account
                List<Check> wrongAccountChecks = checks.stream()
                    .filter(c -> !c.getBankAccountId().equals(request.getBankAccountId()))
                    .toList();
                if (!wrongAccountChecks.isEmpty()) {
                    String wrongNumbers = wrongAccountChecks.stream()
                        .map(Check::getCheckNumber)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");
                    return Mono.error(new BusinessException(
                        "Tous les cheques doivent appartenir au compte bancaire specifie. Cheques invalides: " + wrongNumbers));
                }

                // All validations passed - create the deposit with PENDING status
                return createDepositEntity(request, account, checks);
            });
    }

    /**
     * Creates the deposit entity with PENDING status and assigns checks to it.
     * Note: Check statuses remain RECEIVED - only checkDepositId is set.
     */
    private Mono<CheckDepositResponse> createDepositEntity(
            CreateCheckDepositRequest request, BankAccount account, List<Check> checks) {

        // Calculate total amount and check count
        BigDecimal totalAmount = checks.stream()
            .map(Check::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        int checkCount = checks.size();

        // Generate reference
        return generateDepositReference()
            .flatMap(reference -> {
                LocalDateTime now = LocalDateTime.now();

                CheckDeposit deposit = CheckDeposit.builder()
                    .id(UUID.randomUUID())
                    .reference(reference)
                    .depositDate(request.getDepositDate())
                    .bankAccountId(request.getBankAccountId())
                    .totalAmount(totalAmount)
                    .checkCount(checkCount)
                    .status(STATUS_PENDING) // PENDING - not DEPOSITED
                    .createdAt(now)
                    .updatedAt(now)
                    .isNew(true)
                    .build();

                // Save the deposit
                return depositRepository.save(deposit)
                    .flatMap(savedDeposit ->
                        // Assign checks to deposit (but do NOT change their status)
                        assignChecksToDeposit(checks, savedDeposit)
                            .then(auditLogService.log(
                                AuditModule.CHECK_DEPOSIT,
                                AuditAction.CREATE,
                                savedDeposit.getId(),
                                savedDeposit.getReference(),
                                null,
                                savedDeposit,
                                String.format("Creation de la remise %s (PENDING) : %d cheques pour un total de %.2f %s",
                                    savedDeposit.getReference(), checkCount, totalAmount, account.getCurrency())
                            ))
                            .thenReturn(savedDeposit)
                    )
                    .doOnSuccess(saved -> LOG.info("Check deposit created: id={}, reference={}, status=PENDING, checks={}, total={}",
                        saved.getId(), saved.getReference(), checkCount, totalAmount))
                    .flatMap(this::enrichWithFullDetails);
            });
    }

    /**
     * Assigns checks to the deposit WITHOUT changing their status.
     * Checks remain RECEIVED but are linked via checkDepositId.
     */
    private Mono<Void> assignChecksToDeposit(List<Check> checks, CheckDeposit deposit) {
        return Flux.fromIterable(checks)
            .flatMap(check -> {
                check.setCheckDepositId(deposit.getId());
                // Do NOT change status - check remains RECEIVED
                check.setNew(false);
                return checkRepository.save(check)
                    .flatMap(saved -> auditLogService.log(
                        AuditModule.CHECK,
                        AuditAction.UPDATE,
                        saved.getId(),
                        saved.getCheckNumber(),
                        null,
                        saved,
                        String.format("Cheque assigne a la remise %s (en attente de depot)", deposit.getReference())
                    ).thenReturn(saved));
            })
            .then();
    }

    // =========================================================================
    // STEP 2: CONFIRM DEPOSIT (DEPOSITED)
    // =========================================================================

    /**
     * Confirms that the deposit has been physically deposited at the bank.
     *
     * <p>Workflow Step 2: The deposit transitions from PENDING to DEPOSITED,
     * and all linked checks transition to DEPOSITED status.</p>
     *
     * <p>Validations performed:</p>
     * <ul>
     *   <li>Deposit must be in PENDING status</li>
     *   <li>Deposit date must not be earlier than the most recent receiptDate among the checks</li>
     * </ul>
     *
     * @param depositId the deposit ID
     * @param depositDate the date when the deposit was made at the bank
     * @return Mono of CheckDepositResponse
     */
    public Mono<CheckDepositResponse> confirmDeposit(UUID depositId, LocalDate depositDate) {
        LOG.info("Confirming deposit id={} with depositDate={}", depositId, depositDate);

        return depositRepository.findById(depositId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, depositId)))
            .flatMap(deposit -> {
                // Validation 1: Deposit must be PENDING
                if (!STATUS_PENDING.equals(deposit.getStatus())) {
                    return Mono.error(new BusinessException(
                        String.format("La remise doit etre au statut 'PENDING' pour confirmer le depot. Statut actuel: %s",
                            deposit.getStatus())));
                }

                // Get all checks for validation
                return checkRepository.findByCheckDepositId(depositId)
                    .collectList()
                    .flatMap(checks -> {
                        // Validation 2: depositDate must not be earlier than max receiptDate
                        LocalDate maxReceiptDate = checks.stream()
                            .map(Check::getReceiptDate)
                            .filter(d -> d != null)
                            .max(LocalDate::compareTo)
                            .orElse(null);

                        if (maxReceiptDate != null && depositDate.isBefore(maxReceiptDate)) {
                            return Mono.error(new BusinessException(
                                String.format("La date de depot (%s) ne peut pas etre anterieure a la date de reception la plus recente (%s)",
                                    depositDate, maxReceiptDate)));
                        }

                        // Update deposit
                        CheckDeposit original = deposit.toBuilder().build();
                        deposit.setStatus(STATUS_DEPOSITED);
                        deposit.setDepositDate(depositDate);
                        deposit.setUpdatedAt(LocalDateTime.now());
                        deposit.setNew(false);

                        return depositRepository.save(deposit)
                            .flatMap(savedDeposit ->
                                // Update all checks to DEPOSITED
                                updateChecksToDeposited(checks, savedDeposit, depositDate)
                                    .then(auditLogService.log(
                                        AuditModule.CHECK_DEPOSIT,
                                        AuditAction.DEPOSIT,
                                        savedDeposit.getId(),
                                        savedDeposit.getReference(),
                                        original,
                                        savedDeposit,
                                        String.format("Confirmation du depot de la remise %s en banque le %s",
                                            savedDeposit.getReference(), depositDate)
                                    ))
                                    .thenReturn(savedDeposit)
                            )
                            .doOnSuccess(saved -> LOG.info("Deposit confirmed: id={}, reference={}, status=DEPOSITED",
                                saved.getId(), saved.getReference()))
                            .flatMap(this::enrichWithFullDetails);
                    });
            });
    }

    /**
     * Updates all checks in the deposit to DEPOSITED status.
     */
    private Mono<Void> updateChecksToDeposited(List<Check> checks, CheckDeposit deposit, LocalDate depositDate) {
        return Flux.fromIterable(checks)
            .flatMap(check -> {
                Check original = check.toBuilder().build();
                check.setStatus(STATUS_DEPOSITED);
                check.setDepositDate(depositDate);
                check.setNew(false);
                return checkRepository.save(check)
                    .flatMap(saved -> auditLogService.log(
                        AuditModule.CHECK,
                        AuditAction.DEPOSIT,
                        saved.getId(),
                        saved.getCheckNumber(),
                        original,
                        saved,
                        String.format("Cheque depose en banque via remise %s", deposit.getReference())
                    ).thenReturn(saved));
            })
            .then();
    }

    // =========================================================================
    // STEP 3: CASH DEPOSIT (CASHED)
    // =========================================================================

    /**
     * Marks the deposit as cashed (funds received in account).
     *
     * <p>Workflow Step 3: The deposit transitions from DEPOSITED to CASHED.
     * A single BankTransaction is created for the total amount, the account
     * balance is updated, and all linked checks transition to CASHED status.</p>
     *
     * <p>Validations performed:</p>
     * <ul>
     *   <li>Deposit must be in DEPOSITED status</li>
     *   <li>Cashed date must not be earlier than deposit date</li>
     * </ul>
     *
     * @param depositId the deposit ID
     * @param cashedDate the date when funds were received
     * @return Mono of CheckDepositResponse
     */
    public Mono<CheckDepositResponse> cashDeposit(UUID depositId, LocalDate cashedDate) {
        LOG.info("Cashing deposit id={} with cashedDate={}", depositId, cashedDate);

        return depositRepository.findById(depositId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, depositId)))
            .flatMap(deposit -> {
                // Validation 1: Deposit must be DEPOSITED
                if (!STATUS_DEPOSITED.equals(deposit.getStatus())) {
                    return Mono.error(new BusinessException(
                        String.format("La remise doit etre au statut 'DEPOSITED' pour l'encaisser. Statut actuel: %s",
                            deposit.getStatus())));
                }

                // Validation 2: cashedDate must not be earlier than depositDate
                if (deposit.getDepositDate() != null && cashedDate.isBefore(deposit.getDepositDate())) {
                    return Mono.error(new BusinessException(
                        String.format("La date d'encaissement (%s) ne peut pas etre anterieure a la date de depot (%s)",
                            cashedDate, deposit.getDepositDate())));
                }

                // Get bank account for transaction creation
                return accountRepository.findById(deposit.getBankAccountId())
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankAccount", deposit.getBankAccountId())))
                    .flatMap(account ->
                        // Create the bank transaction
                        createTransactionForDeposit(deposit, account, cashedDate)
                            .flatMap(transaction -> {
                                // Update deposit
                                CheckDeposit original = deposit.toBuilder().build();
                                deposit.setStatus(STATUS_CASHED);
                                deposit.setCashedDate(cashedDate);
                                deposit.setBankTransactionId(transaction.getId());
                                deposit.setUpdatedAt(LocalDateTime.now());
                                deposit.setNew(false);

                                return depositRepository.save(deposit)
                                    .flatMap(savedDeposit ->
                                        // Update all checks to CASHED
                                        checkRepository.findByCheckDepositId(depositId)
                                            .collectList()
                                            .flatMap(checks -> updateChecksToCashed(checks, savedDeposit, cashedDate))
                                            .then(
                                                // Update account balance
                                                updateAccountBalance(account, deposit.getTotalAmount())
                                            )
                                            .then(auditLogService.log(
                                                AuditModule.CHECK_DEPOSIT,
                                                AuditAction.CASH,
                                                savedDeposit.getId(),
                                                savedDeposit.getReference(),
                                                original,
                                                savedDeposit,
                                                String.format("Encaissement de la remise %s - Transaction %s creee - Montant: %.2f %s",
                                                    savedDeposit.getReference(), transaction.getReference(),
                                                    deposit.getTotalAmount(), account.getCurrency())
                                            ))
                                            .thenReturn(savedDeposit)
                                    );
                            })
                    )
                    .doOnSuccess(saved -> LOG.info("Deposit cashed: id={}, reference={}, status=CASHED, transactionId={}",
                        saved.getId(), saved.getReference(), saved.getBankTransactionId()))
                    .flatMap(this::enrichWithFullDetails);
            });
    }

    /**
     * Creates a VALIDATED bank transaction for the deposit.
     */
    private Mono<BankTransaction> createTransactionForDeposit(CheckDeposit deposit, BankAccount account, LocalDate cashedDate) {
        return findOrCreateCheckTransactionType()
            .flatMap(transactionType ->
                generateTransactionReference()
                    .flatMap(reference -> {
                        BankTransaction transaction = BankTransaction.builder()
                            .id(UUID.randomUUID())
                            .bankAccountId(deposit.getBankAccountId())
                            .transactionTypeId(transactionType.getId())
                            .reference(reference)
                            .transactionDate(cashedDate)
                            .valueDate(cashedDate)
                            .amount(deposit.getTotalAmount())
                            .direction("CREDIT") // Deposit = money coming in
                            .description(String.format("Encaissement remise de cheques %s (%d cheques)",
                                deposit.getReference(), deposit.getCheckCount()))
                            .status("VALIDATED") // Auto-validated
                            .systemDate(LocalDateTime.now())
                            .isReconciled(false)
                            .isNew(true)
                            .build();

                        return transactionRepository.save(transaction)
                            .doOnSuccess(saved -> LOG.info(
                                "Transaction created for deposit: txId={}, depositId={}, reference={}, amount={}",
                                saved.getId(), deposit.getId(), saved.getReference(), deposit.getTotalAmount()))
                            .flatMap(saved -> auditLogService.log(
                                AuditModule.BANK_TRANSACTION,
                                AuditAction.CREATE,
                                saved.getId(),
                                saved.getReference(),
                                null,
                                saved,
                                String.format("Creation automatique de transaction pour remise %s", deposit.getReference())
                            ).thenReturn(saved));
                    })
            );
    }

    /**
     * Updates all checks in the deposit to CASHED status.
     */
    private Mono<Void> updateChecksToCashed(List<Check> checks, CheckDeposit deposit, LocalDate cashedDate) {
        return Flux.fromIterable(checks)
            .flatMap(check -> {
                Check original = check.toBuilder().build();
                check.setStatus(STATUS_CASHED);
                check.setCashedDate(cashedDate);
                check.setNew(false);
                return checkRepository.save(check)
                    .flatMap(saved -> auditLogService.log(
                        AuditModule.CHECK,
                        AuditAction.CASH,
                        saved.getId(),
                        saved.getCheckNumber(),
                        original,
                        saved,
                        String.format("Cheque encaisse via remise %s", deposit.getReference())
                    ).thenReturn(saved));
            })
            .then();
    }

    /**
     * Updates the bank account balance (CREDIT = add money).
     */
    private Mono<Void> updateAccountBalance(BankAccount account, BigDecimal amount) {
        BigDecimal currentBalance = account.getCurrentBalance() != null
            ? account.getCurrentBalance()
            : BigDecimal.ZERO;

        BigDecimal newBalance = currentBalance.add(amount);
        account.setCurrentBalance(newBalance);
        account.setNew(false);

        LOG.info("Updating account {} balance: {} + {} = {}",
            account.getId(), currentBalance, amount, newBalance);

        return accountRepository.save(account).then();
    }

    // =========================================================================
    // CANCEL DEPOSIT (OPTIONAL)
    // =========================================================================

    /**
     * Cancels a PENDING deposit and releases the assigned checks.
     *
     * <p>Only deposits in PENDING status can be cancelled.
     * All assigned checks will have their checkDepositId cleared.</p>
     *
     * @param depositId the deposit ID
     * @return Mono<Void>
     */
    public Mono<Void> cancelDeposit(UUID depositId) {
        LOG.info("Cancelling deposit id={}", depositId);

        return depositRepository.findById(depositId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, depositId)))
            .flatMap(deposit -> {
                // Only PENDING deposits can be cancelled
                if (!STATUS_PENDING.equals(deposit.getStatus())) {
                    return Mono.error(new BusinessException(
                        String.format("Seules les remises au statut 'PENDING' peuvent etre annulees. Statut actuel: %s",
                            deposit.getStatus())));
                }

                // Release all checks (clear checkDepositId)
                return checkRepository.findByCheckDepositId(depositId)
                    .flatMap(check -> {
                        check.setCheckDepositId(null);
                        check.setNew(false);
                        return checkRepository.save(check)
                            .flatMap(saved -> auditLogService.log(
                                AuditModule.CHECK,
                                AuditAction.UPDATE,
                                saved.getId(),
                                saved.getCheckNumber(),
                                null,
                                saved,
                                String.format("Cheque libere suite a l'annulation de la remise %s", deposit.getReference())
                            ).thenReturn(saved));
                    })
                    .then(
                        auditLogService.log(
                            AuditModule.CHECK_DEPOSIT,
                            AuditAction.CANCEL,
                            deposit.getId(),
                            deposit.getReference(),
                            deposit,
                            null,
                            String.format("Annulation de la remise %s", deposit.getReference())
                        )
                    )
                    .then(depositRepository.delete(deposit))
                    .doOnSuccess(v -> LOG.info("Deposit cancelled: id={}, reference={}",
                        depositId, deposit.getReference()));
            });
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Generates a unique reference for the deposit.
     * Format: REM-YYYYMM-NNNN
     */
    private Mono<String> generateDepositReference() {
        String yearMonth = LocalDate.now().format(YEAR_MONTH_FORMATTER);

        return sequenceRepository.incrementSequence(DEPOSIT_TYPE_CODE, yearMonth)
            .then(sequenceRepository.findLastSequence(DEPOSIT_TYPE_CODE, yearMonth))
            .map(sequence -> {
                String formattedSequence = String.format("%04d", sequence);
                return DEPOSIT_TYPE_CODE + "-" + yearMonth + "-" + formattedSequence;
            })
            .switchIfEmpty(Mono.error(new BusinessException(
                "Impossible de generer une reference pour la remise.")));
    }

    /**
     * Generates a unique reference for the bank transaction.
     * Format: CHQ-YYYYMM-NNNN
     */
    private Mono<String> generateTransactionReference() {
        String yearMonth = LocalDate.now().format(YEAR_MONTH_FORMATTER);

        return sequenceRepository.incrementSequence(CHECK_TYPE_CODE, yearMonth)
            .then(sequenceRepository.findLastSequence(CHECK_TYPE_CODE, yearMonth))
            .map(sequence -> {
                String formattedSequence = String.format("%04d", sequence);
                return CHECK_TYPE_CODE + "-" + yearMonth + "-" + formattedSequence;
            })
            .switchIfEmpty(Mono.error(new BusinessException(
                "Impossible de generer une reference pour la transaction.")));
    }

    /**
     * Finds or creates the transaction type for check deposits.
     */
    private Mono<TransactionType> findOrCreateCheckTransactionType() {
        return transactionTypeRepository.findByCode(CHECK_TYPE_CODE)
            .switchIfEmpty(
                transactionTypeRepository.findByCategory("CHECK")
                    .next()
                    .switchIfEmpty(
                        Mono.defer(() -> {
                            LOG.warn("No CHECK transaction type found, creating default");
                            TransactionType defaultType = TransactionType.builder()
                                .id(UUID.randomUUID())
                                .code(CHECK_TYPE_CODE)
                                .label("Cheque")
                                .category("CHECK")
                                .description("Transaction par cheque")
                                .isActive(true)
                                .isNew(true)
                                .build();
                            return transactionTypeRepository.save(defaultType);
                        })
                    )
            );
    }

    /**
     * Enriches a CheckDeposit with bank account details.
     */
    private Mono<CheckDepositResponse> enrichWithAccountDetails(CheckDeposit deposit) {
        return accountRepository.findById(deposit.getBankAccountId())
            .map(account -> depositMapper.toResponseWithAccountDetails(
                deposit, account.getName(), account.getCurrency()))
            .defaultIfEmpty(depositMapper.toResponse(deposit));
    }

    /**
     * Enriches a CheckDeposit with full details including checks and transaction reference.
     */
    private Mono<CheckDepositResponse> enrichWithFullDetails(CheckDeposit deposit) {
        Mono<BankAccount> accountMono = accountRepository.findById(deposit.getBankAccountId())
            .defaultIfEmpty(BankAccount.builder().name("Compte inconnu").currency("EUR").build());

        Mono<String> transactionRefMono = deposit.getBankTransactionId() != null
            ? transactionRepository.findById(deposit.getBankTransactionId())
                .map(tx -> tx.getReference())
                .defaultIfEmpty("")
            : Mono.just("");

        Mono<List<CheckResponse>> checksMono = checkRepository.findByCheckDepositId(deposit.getId())
            .flatMap(check -> accountRepository.findById(check.getBankAccountId())
                .map(account -> checkMapper.toResponseWithAccountName(check, account.getName(), account.getCurrency()))
                .defaultIfEmpty(checkMapper.toResponse(check)))
            .collectList();

        return Mono.zip(accountMono, transactionRefMono, checksMono)
            .map(tuple -> depositMapper.toResponseWithFullDetails(
                deposit,
                tuple.getT1().getName(),
                tuple.getT1().getCurrency(),
                tuple.getT2(),
                tuple.getT3()));
    }
}
