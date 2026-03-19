package com.rtcomops.treasury.application.service;

import com.rtcomops.treasury.application.dto.request.CreateCheckDepositRequest;
import com.rtcomops.treasury.application.dto.response.CheckDepositResponse;
import com.rtcomops.treasury.application.dto.response.CheckResponse;
import com.rtcomops.treasury.application.mapper.CheckDepositMapper;
import com.rtcomops.treasury.application.mapper.CheckMapper;
import com.rtcomops.treasury.domain.exception.BusinessException;
import com.rtcomops.treasury.domain.exception.ResourceNotFoundException;
import com.rtcomops.treasury.domain.model.BankAccount;
import com.rtcomops.treasury.domain.model.BankTransaction;
import com.rtcomops.treasury.domain.model.Check;
import com.rtcomops.treasury.domain.model.CheckDeposit;
import com.rtcomops.treasury.domain.model.TransactionType;
import com.rtcomops.treasury.domain.model.enums.AuditAction;
import com.rtcomops.treasury.domain.model.enums.AuditModule;
import com.rtcomops.treasury.application.port.in.AuditLogUseCase;
import com.rtcomops.treasury.application.port.in.CheckDepositUseCase;
import com.rtcomops.treasury.domain.port.out.BankAccountRepositoryPort;
import com.rtcomops.treasury.domain.port.out.BankTransactionRepositoryPort;
import com.rtcomops.treasury.domain.port.out.CheckDepositRepositoryPort;
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
import java.util.List;
import java.util.UUID;

/**
 * Domain service for CheckDeposit operations.
 *
 * <p>Manages batch check deposits (remises de cheques en lot) with a 3-step workflow:</p>
 * <ol>
 *   <li><strong>Create (PENDING)</strong>: Checks are assigned to the deposit but remain RECEIVED</li>
 *   <li><strong>Confirm Deposit (DEPOSITED)</strong>: Checks are physically deposited at the bank</li>
 *   <li><strong>Cash (CASHED)</strong>: Funds are received, bank transaction created</li>
 * </ol>
 */
@Service
@Transactional
public class CheckDepositService implements CheckDepositUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(CheckDepositService.class);
    private static final String RESOURCE_NAME = "CheckDeposit";
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final String DEPOSIT_TYPE_CODE = "REM";
    private static final String CHECK_TYPE_CODE = "CHQ";

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_DEPOSITED = "DEPOSITED";
    private static final String STATUS_CASHED = "CASHED";

    private final CheckDepositRepositoryPort depositRepositoryPort;
    private final CheckRepositoryPort checkRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final BankTransactionRepositoryPort bankTransactionRepositoryPort;
    private final TransactionTypeRepositoryPort transactionTypeRepositoryPort;
    private final TransactionSequenceRepositoryPort transactionSequenceRepositoryPort;
    private final CheckDepositMapper depositMapper;
    private final CheckMapper checkMapper;
    private final AuditLogUseCase auditLogUseCase;

    public CheckDepositService(
            CheckDepositRepositoryPort depositRepositoryPort,
            CheckRepositoryPort checkRepositoryPort,
            BankAccountRepositoryPort bankAccountRepositoryPort,
            BankTransactionRepositoryPort bankTransactionRepositoryPort,
            TransactionTypeRepositoryPort transactionTypeRepositoryPort,
            TransactionSequenceRepositoryPort transactionSequenceRepositoryPort,
            CheckDepositMapper depositMapper,
            CheckMapper checkMapper,
            AuditLogUseCase auditLogUseCase) {
        this.depositRepositoryPort = depositRepositoryPort;
        this.checkRepositoryPort = checkRepositoryPort;
        this.bankAccountRepositoryPort = bankAccountRepositoryPort;
        this.bankTransactionRepositoryPort = bankTransactionRepositoryPort;
        this.transactionTypeRepositoryPort = transactionTypeRepositoryPort;
        this.transactionSequenceRepositoryPort = transactionSequenceRepositoryPort;
        this.depositMapper = depositMapper;
        this.checkMapper = checkMapper;
        this.auditLogUseCase = auditLogUseCase;
    }

    // =========================================================================
    // QUERY METHODS
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public Flux<CheckDepositResponse> findAll() {
        LOG.debug("Finding all check deposits");
        return depositRepositoryPort.findAllOrderByDateDesc()
            .flatMap(this::enrichWithAccountDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<CheckDepositResponse> findByBankAccountId(UUID bankAccountId) {
        LOG.debug("Finding check deposits for account={}", bankAccountId);
        return depositRepositoryPort.findByBankAccountId(bankAccountId)
            .flatMap(this::enrichWithAccountDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<CheckDepositResponse> findById(UUID id) {
        LOG.debug("Finding check deposit by id={}", id);
        return depositRepositoryPort.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, id)))
            .flatMap(this::enrichWithFullDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<CheckDepositResponse> findUnreconciled() {
        LOG.debug("Finding unreconciled check deposits");
        return depositRepositoryPort.findUnreconciled()
            .flatMap(this::enrichWithAccountDetails);
    }

    // =========================================================================
    // STEP 1: CREATE DEPOSIT (PENDING)
    // =========================================================================

    @Override
    public Mono<CheckDepositResponse> createDeposit(CreateCheckDepositRequest request) {
        LOG.info("Creating check deposit for account={} with {} checks",
            request.getBankAccountId(), request.getCheckIds().size());

        return bankAccountRepositoryPort.findById(request.getBankAccountId())
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankAccount", request.getBankAccountId())))
            .flatMap(account -> validateAndCreateDeposit(request, account));
    }

    private Mono<CheckDepositResponse> validateAndCreateDeposit(
            CreateCheckDepositRequest request, BankAccount account) {

        List<UUID> checkIds = request.getCheckIds();

        return checkRepositoryPort.findAllById(checkIds)
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

                // Validation 5: Deposit date must not be earlier than the most recent receipt date
                LocalDate maxReceiptDate = checks.stream()
                    .map(Check::getReceiptDate)
                    .filter(d -> d != null)
                    .max(LocalDate::compareTo)
                    .orElse(null);

                if (maxReceiptDate != null && request.getDepositDate().isBefore(maxReceiptDate)) {
                    return Mono.error(new BusinessException(
                        String.format("La date de remise (%s) ne peut pas etre anterieure a la date de reception la plus recente (%s)",
                            request.getDepositDate(), maxReceiptDate)));
                }

                return createDepositEntity(request, account, checks);
            });
    }

    private Mono<CheckDepositResponse> createDepositEntity(
            CreateCheckDepositRequest request, BankAccount account, List<Check> checks) {

        BigDecimal totalAmount = checks.stream()
            .map(Check::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        int checkCount = checks.size();

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
                    .status(STATUS_PENDING)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

                return depositRepositoryPort.save(deposit)
                    .flatMap(savedDeposit ->
                        assignChecksToDeposit(checks, savedDeposit)
                            .then(auditLogUseCase.log(
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

    private Mono<Void> assignChecksToDeposit(List<Check> checks, CheckDeposit deposit) {
        return Flux.fromIterable(checks)
            .flatMap(check -> {
                check.setCheckDepositId(deposit.getId());
                return checkRepositoryPort.save(check)
                    .flatMap(saved -> auditLogUseCase.log(
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

    @Override
    public Mono<CheckDepositResponse> confirmDeposit(UUID depositId, LocalDate depositDate) {
        LOG.info("Confirming deposit id={} with depositDate={}", depositId, depositDate);

        return depositRepositoryPort.findById(depositId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, depositId)))
            .flatMap(deposit -> {
                if (!STATUS_PENDING.equals(deposit.getStatus())) {
                    return Mono.error(new BusinessException(
                        String.format("La remise doit etre au statut 'PENDING' pour confirmer le depot. Statut actuel: %s",
                            deposit.getStatus())));
                }

                return checkRepositoryPort.findByCheckDepositId(depositId)
                    .collectList()
                    .flatMap(checks -> {
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

                        CheckDeposit original = CheckDeposit.builder()
                            .id(deposit.getId())
                            .reference(deposit.getReference())
                            .status(deposit.getStatus())
                            .build();
                        deposit.setStatus(STATUS_DEPOSITED);
                        deposit.setDepositDate(depositDate);
                        deposit.setUpdatedAt(LocalDateTime.now());

                        return depositRepositoryPort.save(deposit)
                            .flatMap(savedDeposit ->
                                updateChecksToDeposited(checks, savedDeposit, depositDate)
                                    .then(auditLogUseCase.log(
                                        AuditModule.CHECK_DEPOSIT,
                                        AuditAction.DEPOSIT,
                                        savedDeposit.getId(),
                                        savedDeposit.getReference(),
                                        original,
                                        savedDeposit,
                                        String.format("Confirmation du depot de la remise %s en banque le %s",
                                            savedDeposit.getReference(), depositDate)
                                    ))
                                    .then(Mono.just(savedDeposit))
                            )
                            .doOnSuccess(saved -> LOG.info("Deposit confirmed: id={}, reference={}, status=DEPOSITED",
                                saved.getId(), saved.getReference()))
                            .flatMap((CheckDeposit d) -> enrichWithFullDetails(d));
                    });
            });
    }

    private Mono<Void> updateChecksToDeposited(List<Check> checks, CheckDeposit deposit, LocalDate depositDate) {
        return Flux.fromIterable(checks)
            .flatMap(check -> {
                Check original = Check.builder()
                    .id(check.getId())
                    .checkNumber(check.getCheckNumber())
                    .status(check.getStatus())
                    .build();
                check.setStatus(STATUS_DEPOSITED);
                check.setDepositDate(depositDate);
                return checkRepositoryPort.save(check)
                    .flatMap(saved -> auditLogUseCase.log(
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

    @Override
    public Mono<CheckDepositResponse> cashDeposit(UUID depositId, LocalDate cashedDate) {
        LOG.info("Cashing deposit id={} with cashedDate={}", depositId, cashedDate);

        return depositRepositoryPort.findById(depositId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, depositId)))
            .flatMap(deposit -> {
                if (!STATUS_DEPOSITED.equals(deposit.getStatus())) {
                    return Mono.error(new BusinessException(
                        String.format("La remise doit etre au statut 'DEPOSITED' pour l'encaisser. Statut actuel: %s",
                            deposit.getStatus())));
                }

                if (deposit.getDepositDate() != null && cashedDate.isBefore(deposit.getDepositDate())) {
                    return Mono.error(new BusinessException(
                        String.format("La date d'encaissement (%s) ne peut pas etre anterieure a la date de depot (%s)",
                            cashedDate, deposit.getDepositDate())));
                }

                return bankAccountRepositoryPort.findById(deposit.getBankAccountId())
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankAccount", deposit.getBankAccountId())))
                    .flatMap(account ->
                        createTransactionForDeposit(deposit, account, cashedDate)
                            .flatMap(transaction -> {
                                CheckDeposit original = CheckDeposit.builder()
                                    .id(deposit.getId())
                                    .reference(deposit.getReference())
                                    .status(deposit.getStatus())
                                    .build();
                                deposit.setStatus(STATUS_CASHED);
                                deposit.setCashedDate(cashedDate);
                                deposit.setBankTransactionId(transaction.getId());
                                deposit.setUpdatedAt(LocalDateTime.now());

                                return depositRepositoryPort.save(deposit)
                                    .flatMap(savedDeposit ->
                                        checkRepositoryPort.findByCheckDepositId(depositId)
                                            .collectList()
                                            .flatMap(checks -> updateChecksToCashed(checks, savedDeposit, cashedDate))
                                            .then(updateAccountBalance(account, deposit.getTotalAmount()))
                                            .then(auditLogUseCase.log(
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
                            .direction("CREDIT")
                            .description(String.format("Encaissement remise de cheques %s (%d cheques)",
                                deposit.getReference(), deposit.getCheckCount()))
                            .status("VALIDATED")
                            .systemDate(LocalDateTime.now())
                            .isReconciled(false)
                            .build();

                        return bankTransactionRepositoryPort.save(transaction)
                            .doOnSuccess(saved -> LOG.info(
                                "Transaction created for deposit: txId={}, depositId={}, reference={}, amount={}",
                                saved.getId(), deposit.getId(), saved.getReference(), deposit.getTotalAmount()))
                            .flatMap(saved -> auditLogUseCase.log(
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

    private Mono<Void> updateChecksToCashed(List<Check> checks, CheckDeposit deposit, LocalDate cashedDate) {
        return Flux.fromIterable(checks)
            .flatMap(check -> {
                Check original = Check.builder()
                    .id(check.getId())
                    .checkNumber(check.getCheckNumber())
                    .status(check.getStatus())
                    .build();
                check.setStatus(STATUS_CASHED);
                check.setCashedDate(cashedDate);
                return checkRepositoryPort.save(check)
                    .flatMap(saved -> auditLogUseCase.log(
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

    private Mono<Void> updateAccountBalance(BankAccount account, BigDecimal amount) {
        BigDecimal currentBalance = account.getCurrentBalance() != null
            ? account.getCurrentBalance()
            : BigDecimal.ZERO;

        BigDecimal newBalance = currentBalance.add(amount);
        account.setCurrentBalance(newBalance);

        LOG.info("Updating account {} balance: {} + {} = {}",
            account.getId(), currentBalance, amount, newBalance);

        return bankAccountRepositoryPort.save(account).then();
    }

    // =========================================================================
    // CANCEL DEPOSIT
    // =========================================================================

    @Override
    public Mono<Void> cancelDeposit(UUID depositId) {
        LOG.info("Cancelling deposit id={}", depositId);

        return depositRepositoryPort.findById(depositId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NAME, depositId)))
            .flatMap(deposit -> {
                if (!STATUS_PENDING.equals(deposit.getStatus())) {
                    return Mono.error(new BusinessException(
                        String.format("Seules les remises au statut 'PENDING' peuvent etre annulees. Statut actuel: %s",
                            deposit.getStatus())));
                }

                return checkRepositoryPort.findByCheckDepositId(depositId)
                    .flatMap(check -> {
                        check.setCheckDepositId(null);
                        return checkRepositoryPort.save(check)
                            .flatMap(saved -> auditLogUseCase.log(
                                AuditModule.CHECK,
                                AuditAction.UPDATE,
                                saved.getId(),
                                saved.getCheckNumber(),
                                null,
                                saved,
                                String.format("Cheque libere suite a l'annulation de la remise %s", deposit.getReference())
                            ).thenReturn(saved));
                    })
                    .then(auditLogUseCase.log(
                        AuditModule.CHECK_DEPOSIT,
                        AuditAction.CANCEL,
                        deposit.getId(),
                        deposit.getReference(),
                        deposit,
                        null,
                        String.format("Annulation de la remise %s", deposit.getReference())
                    ))
                    .then(depositRepositoryPort.delete(deposit))
                    .doOnSuccess(v -> LOG.info("Deposit cancelled: id={}, reference={}",
                        depositId, deposit.getReference()));
            });
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private Mono<String> generateDepositReference() {
        String yearMonth = LocalDate.now().format(YEAR_MONTH_FORMATTER);

        return transactionSequenceRepositoryPort.incrementSequence(DEPOSIT_TYPE_CODE, yearMonth)
            .then(transactionSequenceRepositoryPort.findLastSequence(DEPOSIT_TYPE_CODE, yearMonth))
            .map(sequence -> {
                String formattedSequence = String.format("%04d", sequence);
                return DEPOSIT_TYPE_CODE + "-" + yearMonth + "-" + formattedSequence;
            })
            .switchIfEmpty(Mono.error(new BusinessException(
                "Impossible de generer une reference pour la remise.")));
    }

    private Mono<String> generateTransactionReference() {
        String yearMonth = LocalDate.now().format(YEAR_MONTH_FORMATTER);

        return transactionSequenceRepositoryPort.incrementSequence(CHECK_TYPE_CODE, yearMonth)
            .then(transactionSequenceRepositoryPort.findLastSequence(CHECK_TYPE_CODE, yearMonth))
            .map(sequence -> {
                String formattedSequence = String.format("%04d", sequence);
                return CHECK_TYPE_CODE + "-" + yearMonth + "-" + formattedSequence;
            })
            .switchIfEmpty(Mono.error(new BusinessException(
                "Impossible de generer une reference pour la transaction.")));
    }

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
                                .label("Cheque")
                                .category("CHECK")
                                .description("Transaction par cheque")
                                .isActive(true)
                                .build();
                            return transactionTypeRepositoryPort.save(defaultType);
                        })
                    )
            );
    }

    private Mono<CheckDepositResponse> enrichWithAccountDetails(CheckDeposit deposit) {
        return bankAccountRepositoryPort.findById(deposit.getBankAccountId())
            .map(account -> depositMapper.toResponseWithAccountDetails(
                deposit, account.getName(), account.getCurrency()))
            .defaultIfEmpty(depositMapper.toResponse(deposit));
    }

    private Mono<CheckDepositResponse> enrichWithFullDetails(CheckDeposit deposit) {
        Mono<BankAccount> accountMono = bankAccountRepositoryPort.findById(deposit.getBankAccountId())
            .defaultIfEmpty(BankAccount.builder().name("Compte inconnu").currency("EUR").build());

        Mono<String> transactionRefMono = deposit.getBankTransactionId() != null
            ? bankTransactionRepositoryPort.findById(deposit.getBankTransactionId())
                .map(BankTransaction::getReference)
                .defaultIfEmpty("")
            : Mono.just("");

        Mono<List<CheckResponse>> checksMono = checkRepositoryPort.findByCheckDepositId(deposit.getId())
            .flatMap(check -> bankAccountRepositoryPort.findById(check.getBankAccountId())
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
