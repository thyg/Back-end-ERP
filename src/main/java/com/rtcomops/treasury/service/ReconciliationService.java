package com.rtcomops.treasury.service;

import com.rtcomops.treasury.dto.request.AutoReconcileRequest;
import com.rtcomops.treasury.dto.request.ReconcileManualRequest;
import com.rtcomops.treasury.dto.response.ReconciliationMatchResponse;
import com.rtcomops.treasury.dto.response.ReconciliationSummaryResponse;
import com.rtcomops.treasury.entity.BankStatement;
import com.rtcomops.treasury.entity.BankTransaction;
import com.rtcomops.treasury.entity.Check;
import com.rtcomops.treasury.entity.CheckDeposit;
import com.rtcomops.treasury.entity.ReconciliationMatch;
import com.rtcomops.treasury.entity.StatementLine;
import com.rtcomops.treasury.enums.AuditAction;
import com.rtcomops.treasury.enums.AuditModule;
import com.rtcomops.treasury.exception.ResourceNotFoundException;
import com.rtcomops.treasury.mapper.ReconciliationMatchMapper;
import com.rtcomops.treasury.mapper.StatementLineMapper;
import com.rtcomops.treasury.repository.BankAccountRepository;
import com.rtcomops.treasury.repository.BankStatementRepository;
import com.rtcomops.treasury.repository.BankTransactionRepository;
import com.rtcomops.treasury.repository.CheckDepositRepository;
import com.rtcomops.treasury.repository.CheckRepository;
import com.rtcomops.treasury.repository.ReconciliationMatchRepository;
import com.rtcomops.treasury.repository.StatementLineRepository;
import com.rtcomops.treasury.repository.TransactionSequenceRepository;
import com.rtcomops.treasury.repository.TransactionTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service layer for reconciliation operations.
 *
 * <p>This service handles both manual and automatic reconciliation between
 * bank statement lines and internal transactions/checks.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Service
@Transactional
public class ReconciliationService {

    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationService.class);
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final String DEPOSIT_TX_TYPE_CODE = "REM";

    private final ReconciliationMatchRepository matchRepository;
    private final StatementLineRepository lineRepository;
    private final BankStatementRepository statementRepository;
    private final BankTransactionRepository transactionRepository;
    private final CheckRepository checkRepository;
    private final CheckDepositRepository depositRepository;
    private final BankAccountRepository accountRepository;
    private final TransactionSequenceRepository sequenceRepository;
    private final TransactionTypeRepository transactionTypeRepository;
    private final ReconciliationMatchMapper matchMapper;
    private final StatementLineMapper lineMapper;
    private final AuditLogService auditLogService;

    public ReconciliationService(
            ReconciliationMatchRepository matchRepository,
            StatementLineRepository lineRepository,
            BankStatementRepository statementRepository,
            BankTransactionRepository transactionRepository,
            CheckRepository checkRepository,
            CheckDepositRepository depositRepository,
            BankAccountRepository accountRepository,
            TransactionSequenceRepository sequenceRepository,
            TransactionTypeRepository transactionTypeRepository,
            ReconciliationMatchMapper matchMapper,
            StatementLineMapper lineMapper,
            AuditLogService auditLogService) {
        this.matchRepository = matchRepository;
        this.lineRepository = lineRepository;
        this.statementRepository = statementRepository;
        this.transactionRepository = transactionRepository;
        this.checkRepository = checkRepository;
        this.depositRepository = depositRepository;
        this.accountRepository = accountRepository;
        this.sequenceRepository = sequenceRepository;
        this.transactionTypeRepository = transactionTypeRepository;
        this.matchMapper = matchMapper;
        this.lineMapper = lineMapper;
        this.auditLogService = auditLogService;
    }

    /**
     * Performs manual reconciliation between a statement line and a transaction or check.
     *
     * @param request the manual reconciliation request
     * @return mono of reconciliation match response
     */
    public Mono<ReconciliationMatchResponse> reconcileManual(ReconcileManualRequest request) {
        LOG.info("Performing manual reconciliation for line={}", request.getStatementLineId());
        
        return lineRepository.findById(request.getStatementLineId())
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("StatementLine", request.getStatementLineId())))
            .flatMap(line -> {
                Mono<BigDecimal> amountMono;
                String entityType;
                UUID internalId;

                if (request.getBankTransactionId() != null) {
                    entityType = "Transaction";
                    internalId = request.getBankTransactionId();
                    amountMono = transactionRepository.findById(request.getBankTransactionId())
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankTransaction", request.getBankTransactionId())))
                        .flatMap(tx -> markTransactionAsReconciled(tx).thenReturn(tx.getAmount()));
                } else if (request.getCheckId() != null) {
                    entityType = "Check";
                    internalId = request.getCheckId();
                    amountMono = checkRepository.findById(request.getCheckId())
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Check", request.getCheckId())))
                        .map(Check::getAmount);
                } else {
                    return Mono.error(new IllegalArgumentException("Either bankTransactionId or checkId must be provided"));
                }
                
                return amountMono.flatMap(amount -> {
                    BigDecimal matchedAmount = request.getMatchedAmount() != null 
                        ? request.getMatchedAmount() 
                        : amount;
                    
                    ReconciliationMatch match = matchMapper.toEntityFromManual(request, matchedAmount);
                    
                    return matchRepository.save(match)
                        .flatMap(savedMatch -> auditLogService.log(
                                AuditModule.RECONCILIATION,
                                AuditAction.CREATE,
                                savedMatch.getId(),
                                "Rapprochement Manuel",
                                null,
                                savedMatch,
                                String.format("Ligne %.2f rapprochée avec %s %s", line.getAmount(), entityType, internalId)
                        ).thenReturn(savedMatch))
                        .flatMap(savedMatch -> {
                            StatementLine updatedLine = lineMapper.markAsMatched(line);
                            return lineRepository.save(updatedLine)
                                .flatMap(sl -> updateStatementReconciledCount(sl.getBankStatementId()))
                                .thenReturn(matchMapper.toResponse(savedMatch));
                        });
                });
            });
    }

    /**
     * Removes a reconciliation match and resets the statement line.
     *
     * @param matchId the match ID
     * @return mono of void
     */
    public Mono<Void> unmatch(UUID matchId) {
        LOG.info("Removing reconciliation match id={}", matchId);
        
        return matchRepository.findById(matchId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("ReconciliationMatch", matchId)))
            .flatMap(match -> 
                auditLogService.log(
                    AuditModule.RECONCILIATION,
                    AuditAction.DELETE,
                    match.getId(),
                    "Annulation Rapprochement",
                    match,
                    null,
                    "Annulation du rapprochement pour la ligne de relevé " + match.getStatementLineId()
                ).then(Mono.defer(() -> {
                    Mono<Void> unmarkTransaction = Mono.empty();
                    if (match.getBankTransactionId() != null) {
                        unmarkTransaction = transactionRepository.findById(match.getBankTransactionId())
                            .flatMap(this::unmarkTransactionAsReconciled)
                            .then();
                    }
                    
                    return unmarkTransaction
                        .then(lineRepository.findById(match.getStatementLineId()))
                        .flatMap(line -> {
                            StatementLine updated = lineMapper.markAsUnmatched(line);
                            return lineRepository.save(updated);
                        })
                        .flatMap(line -> updateStatementReconciledCount(line.getBankStatementId()))
                        .then(matchRepository.delete(match));
                }))
            );
    }

    /**
     * Performs automatic reconciliation for a bank statement.
     *
     * @param request the auto reconcile request
     * @return flux of reconciliation match responses
     */
    public Flux<ReconciliationMatchResponse> reconcileAuto(AutoReconcileRequest request) {
        LOG.info("Performing automatic reconciliation for statement={}", request.getBankStatementId());
        
        return statementRepository.findById(request.getBankStatementId())
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankStatement", request.getBankStatementId())))
            .flatMapMany(statement -> 
                lineRepository.findUnmatchedByStatementId(statement.getId())
                    .flatMap(line -> findMatchingTransaction(line, statement.getBankAccountId(), request)
                        .flatMap(match -> {
                            if (match.getConfidenceScore().compareTo(request.getMinimumConfidenceScore()) >= 0) {
                                return matchRepository.save(match)
                                    .flatMap(savedMatch -> auditLogService.log(
                                        AuditModule.RECONCILIATION,
                                        AuditAction.CREATE,
                                        savedMatch.getId(),
                                        "Rapprochement Auto",
                                        null,
                                        savedMatch,
                                        String.format("Ligne %.2f auto-rapprochée (Confiance: %.2f%%)", 
                                            line.getAmount(), savedMatch.getConfidenceScore())
                                    ).thenReturn(savedMatch))
                                    .flatMap(savedMatch -> {
                                        StatementLine updatedLine = lineMapper.markAsMatched(line);
                                        return lineRepository.save(updatedLine)
                                            .thenReturn(matchMapper.toResponse(savedMatch));
                                    });
                            }
                            return Mono.empty();
                        })
                    )
            )
            .doOnComplete(() -> 
                statementRepository.findById(request.getBankStatementId())
                    .flatMap(st -> {
                        st.setStatus("IN_PROGRESS");
                        st.setNew(false);
                        return statementRepository.save(st);
                    })
                    .subscribe()
            );
    }

    /**
     * Gets reconciliation summary for a bank statement.
     *
     * @param statementId the statement ID
     * @return mono of reconciliation summary response
     */
    @Transactional(readOnly = true)
    public Mono<ReconciliationSummaryResponse> getSummary(UUID statementId) {
        LOG.debug("Getting reconciliation summary for statement={}", statementId);
        
        return statementRepository.findById(statementId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("BankStatement", statementId)))
            .flatMap(statement -> 
                accountRepository.findById(statement.getBankAccountId())
                    .flatMap(account -> 
                        lineRepository.findByBankStatementIdOrderByLineNumber(statementId)
                            .collectList()
                            .map(lines -> buildSummary(statement, account.getName(), lines))
                    )
            );
    }

    /**
     * Gets matches for a statement line.
     *
     * @param lineId the line ID
     * @return flux of reconciliation match responses
     */
    @Transactional(readOnly = true)
    public Flux<ReconciliationMatchResponse> findMatchesByLineId(UUID lineId) {
        LOG.debug("Finding matches for line={}", lineId);
        return matchRepository.findByStatementLineId(lineId)
            .flatMap(this::enrichMatchWithDetails);
    }

    /**
     * Finds a matching transaction for a statement line.
     */
    private Mono<ReconciliationMatch> findMatchingTransaction(
            StatementLine line, UUID accountId, AutoReconcileRequest request) {
        
        return transactionRepository.findUnreconciledByAccountId(accountId)
            .filter(tx -> isMatch(line, tx, request))
            .next()
            .flatMap(tx -> {
                BigDecimal confidence = calculateConfidence(line, tx, request);
                
                return markTransactionAsReconciled(tx)
                    .thenReturn(matchMapper.toEntityFromAuto(
                        line.getId(),
                        tx.getId(),
                        null,
                        line.getAmount(),
                        confidence,
                        confidence.compareTo(BigDecimal.valueOf(100)) == 0 ? "AUTO_EXACT" : "AUTO_FUZZY"
                    ));
            });
    }

    /**
     * Checks if a transaction matches a statement line based on criteria.
     */
    private boolean isMatch(StatementLine line, BankTransaction tx, AutoReconcileRequest request) {
        boolean amountMatch = true;
        boolean directionMatch = line.getDirection().equals(tx.getDirection());
        
        if (request.getMatchByAmount()) {
            BigDecimal diff = line.getAmount().subtract(tx.getAmount()).abs();
            amountMatch = diff.compareTo(request.getAmountTolerance()) <= 0;
        }
        
        boolean dateMatch = true;
        if (request.getMatchByDate()) {
            long daysDiff = Math.abs(
                line.getTransactionDate().toEpochDay() - tx.getTransactionDate().toEpochDay()
            );
            dateMatch = daysDiff <= request.getDateToleranceDays();
        }
        
        return amountMatch && directionMatch && dateMatch;
    }

    /**
     * Calculates confidence score for a match.
     */
    private BigDecimal calculateConfidence(StatementLine line, BankTransaction tx, AutoReconcileRequest request) {
        BigDecimal score = BigDecimal.ZERO;
        int factors = 0;
        
        // Amount match (40 points max)
        if (request.getMatchByAmount()) {
            if (line.getAmount().compareTo(tx.getAmount()) == 0) {
                score = score.add(BigDecimal.valueOf(40));
            } else {
                BigDecimal diff = line.getAmount().subtract(tx.getAmount()).abs();
                BigDecimal ratio = BigDecimal.ONE.subtract(
                    diff.divide(line.getAmount(), 4, RoundingMode.HALF_UP)
                );
                score = score.add(ratio.multiply(BigDecimal.valueOf(40)));
            }
            factors++;
        }
        
        // Date match (30 points max)
        if (request.getMatchByDate()) {
            long daysDiff = Math.abs(
                line.getTransactionDate().toEpochDay() - tx.getTransactionDate().toEpochDay()
            );
            if (daysDiff == 0) {
                score = score.add(BigDecimal.valueOf(30));
            } else {
                BigDecimal ratio = BigDecimal.ONE.subtract(
                    BigDecimal.valueOf(daysDiff).divide(BigDecimal.valueOf(request.getDateToleranceDays()), 4, RoundingMode.HALF_UP)
                );
                score = score.add(ratio.max(BigDecimal.ZERO).multiply(BigDecimal.valueOf(30)));
            }
            factors++;
        }
        
        // Reference match (30 points max)
        if (request.getMatchByReference() && line.getReference() != null && tx.getReference() != null) {
            if (line.getReference().equalsIgnoreCase(tx.getReference())) {
                score = score.add(BigDecimal.valueOf(30));
            }
            factors++;
        }
        
        // Normalize score
        if (factors > 0 && factors < 3) {
            score = score.multiply(BigDecimal.valueOf(100)).divide(
                BigDecimal.valueOf(factors * (100 / 3)), 2, RoundingMode.HALF_UP
            );
        }
        
        return score.min(BigDecimal.valueOf(100));
    }

    /**
     * Marks a transaction as reconciled.
     */
    private Mono<BankTransaction> markTransactionAsReconciled(BankTransaction tx) {
        tx.setIsReconciled(true);
        tx.setReconciledAt(LocalDateTime.now());
        tx.setNew(false);
        return transactionRepository.save(tx);
    }

    /**
     * Unmarks a transaction as reconciled.
     */
    private Mono<BankTransaction> unmarkTransactionAsReconciled(BankTransaction tx) {
        tx.setIsReconciled(false);
        tx.setReconciledAt(null);
        tx.setNew(false);
        return transactionRepository.save(tx);
    }

    /**
     * Updates the reconciled count in a bank statement.
     */
    private Mono<Void> updateStatementReconciledCount(UUID statementId) {
        return lineRepository.countMatchedByStatementId(statementId)
            .flatMap(count -> statementRepository.findById(statementId)
                .flatMap(statement -> {
                    statement.setReconciledCount(count.intValue());
                    statement.setNew(false);
                    return statementRepository.save(statement);
                }))
            .then();
    }

    /**
     * Enriches a match with transaction/check details.
     */
    private Mono<ReconciliationMatchResponse> enrichMatchWithDetails(ReconciliationMatch match) {
        Mono<String> txRefMono = match.getBankTransactionId() != null
            ? transactionRepository.findById(match.getBankTransactionId())
                .map(BankTransaction::getReference)
                .defaultIfEmpty("")
            : Mono.just("");
        
        Mono<String> checkNumMono = match.getCheckId() != null
            ? checkRepository.findById(match.getCheckId())
                .map(Check::getCheckNumber)
                .defaultIfEmpty("")
            : Mono.just("");
        
        return Mono.zip(txRefMono, checkNumMono)
            .map(tuple -> matchMapper.toResponseWithDetails(match, tuple.getT1(), tuple.getT2()));
    }

    /**
     * Builds a reconciliation summary from statement and lines.
     */
    private ReconciliationSummaryResponse buildSummary(
            BankStatement statement, String accountName, java.util.List<StatementLine> lines) {
        
        int matchedLines = 0;
        int unmatchedLines = 0;
        int ignoredLines = 0;
        BigDecimal matchedCredits = BigDecimal.ZERO;
        BigDecimal matchedDebits = BigDecimal.ZERO;
        BigDecimal unmatchedCredits = BigDecimal.ZERO;
        BigDecimal unmatchedDebits = BigDecimal.ZERO;
        
        for (StatementLine line : lines) {
            boolean isCredit = "CREDIT".equals(line.getDirection());
            
            switch (line.getReconciliationStatus()) {
                case "MATCHED":
                    matchedLines++;
                    if (isCredit) matchedCredits = matchedCredits.add(line.getAmount());
                    else matchedDebits = matchedDebits.add(line.getAmount());
                    break;
                case "IGNORED":
                    ignoredLines++;
                    break;
                default:
                    unmatchedLines++;
                    if (isCredit) unmatchedCredits = unmatchedCredits.add(line.getAmount());
                    else unmatchedDebits = unmatchedDebits.add(line.getAmount());
            }
        }
        
        int totalLines = lines.size();
        BigDecimal progress = totalLines > 0
            ? BigDecimal.valueOf(matchedLines + ignoredLines)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalLines), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        BigDecimal calculatedBalance = statement.getOpeningBalance()
            .add(statement.getTotalCredits())
            .subtract(statement.getTotalDebits());
        
        BigDecimal balanceDifference = statement.getClosingBalance().subtract(calculatedBalance);
        
        return ReconciliationSummaryResponse.builder()
            .bankStatementId(statement.getId())
            .bankAccountName(accountName)
            .totalLines(totalLines)
            .matchedLines(matchedLines)
            .unmatchedLines(unmatchedLines)
            .ignoredLines(ignoredLines)
            .progressPercentage(progress)
            .totalCredits(statement.getTotalCredits())
            .totalDebits(statement.getTotalDebits())
            .matchedCredits(matchedCredits)
            .matchedDebits(matchedDebits)
            .unmatchedCredits(unmatchedCredits)
            .unmatchedDebits(unmatchedDebits)
            .openingBalance(statement.getOpeningBalance())
            .closingBalance(statement.getClosingBalance())
            .calculatedBalance(calculatedBalance)
            .balanceDifference(balanceDifference)
            .status(statement.getStatus())
            .build();
    }

    // =========================================================================
    // CHECK DEPOSIT RECONCILIATION (REMISES EN LOT)
    // =========================================================================

    /**
     * Reconciles a check deposit batch with a bank statement line.
     *
     * <p>This method:</p>
     * <ul>
     *   <li>Finds the CheckDeposit by its ID</li>
     *   <li>Changes its status to RECONCILED</li>
     *   <li>Creates a unique BankTransaction for the total amount</li>
     *   <li>Links the transaction to the deposit</li>
     *   <li>Updates all associated checks to CASHED status</li>
     *   <li>Updates the bank account balance once with the total amount</li>
     *   <li>Creates a ReconciliationMatch between the transaction and the statement line</li>
     * </ul>
     *
     * @param depositId the ID of the check deposit to reconcile
     * @param statementLineId the ID of the bank statement line to match with
     * @return Mono<Void> completing when reconciliation is done
     */
    public Mono<Void> reconcileCheckDeposit(UUID depositId, UUID statementLineId) {
        LOG.info("Reconciling check deposit id={} with statement line id={}", depositId, statementLineId);

        return depositRepository.findById(depositId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("CheckDeposit", depositId)))
            .flatMap(deposit -> {
                // Validate deposit status
                if ("RECONCILED".equals(deposit.getStatus())) {
                    return Mono.error(new com.rtcomops.treasury.exception.BusinessException(
                        "Cette remise de chèques a déjà été rapprochée"));
                }

                // Find the statement line
                return lineRepository.findById(statementLineId)
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("StatementLine", statementLineId)))
                    .flatMap(line -> reconcileDepositWithLine(deposit, line));
            });
    }

    /**
     * Performs the actual reconciliation of a deposit with a statement line.
     */
    private Mono<Void> reconcileDepositWithLine(CheckDeposit deposit, StatementLine line) {
        LocalDate reconciliationDate = line.getTransactionDate();

        // Create the bank transaction
        return createTransactionForDeposit(deposit, reconciliationDate)
            .flatMap(transaction -> {
                // Update deposit status and link to transaction
                deposit.setStatus("RECONCILED");
                deposit.setBankTransactionId(transaction.getId());
                deposit.setNew(false);

                return depositRepository.save(deposit)
                    .flatMap(savedDeposit ->
                        // Update all associated checks to CASHED
                        cashChecksInDeposit(savedDeposit.getId(), reconciliationDate)
                            .then(
                                // Update bank account balance once
                                updateAccountBalanceForDeposit(savedDeposit.getBankAccountId(), savedDeposit.getTotalAmount())
                            )
                            .then(
                                // Create reconciliation match
                                createMatchForDeposit(transaction, line)
                            )
                            .then(
                                // Mark statement line as matched
                                markLineAsMatched(line)
                            )
                            .then(
                                // Log the reconciliation
                                auditLogService.log(
                                    AuditModule.CHECK_DEPOSIT,
                                    AuditAction.RECONCILE,
                                    savedDeposit.getId(),
                                    savedDeposit.getReference(),
                                    null,
                                    savedDeposit,
                                    String.format("Rapprochement de la remise %s - Transaction %s créée - %d chèques encaissés",
                                        savedDeposit.getReference(), transaction.getReference(), savedDeposit.getCheckCount())
                                )
                            )
                    );
            })
            .doOnSuccess(v -> LOG.info("Check deposit reconciliation completed: depositId={}", deposit.getId()));
    }

    /**
     * Creates a bank transaction for the deposit.
     */
    private Mono<BankTransaction> createTransactionForDeposit(CheckDeposit deposit, LocalDate transactionDate) {
        return findOrCreateDepositTransactionType()
            .flatMap(transactionType ->
                generateDepositTransactionReference()
                    .flatMap(reference -> {
                        BankTransaction transaction = BankTransaction.builder()
                            .id(UUID.randomUUID())
                            .bankAccountId(deposit.getBankAccountId())
                            .transactionTypeId(transactionType.getId())
                            .reference(reference)
                            .transactionDate(transactionDate)
                            .valueDate(transactionDate)
                            .amount(deposit.getTotalAmount())
                            .direction("CREDIT") // Deposit is always a credit
                            .description("Remise de chèques " + deposit.getReference() +
                                " (" + deposit.getCheckCount() + " chèques)")
                            .status("VALIDATED")
                            .systemDate(LocalDateTime.now())
                            .isReconciled(true)
                            .reconciledAt(LocalDateTime.now())
                            .isNew(true)
                            .build();

                        return transactionRepository.save(transaction)
                            .doOnSuccess(saved -> LOG.info(
                                "Transaction created for deposit: txId={}, depositId={}, reference={}",
                                saved.getId(), deposit.getId(), saved.getReference()))
                            .flatMap(saved -> auditLogService.log(
                                AuditModule.BANK_TRANSACTION,
                                AuditAction.CREATE,
                                saved.getId(),
                                saved.getReference(),
                                null,
                                saved,
                                "Création automatique de transaction pour remise de chèques " + deposit.getReference()
                            ).thenReturn(saved));
                    })
            );
    }

    /**
     * Finds or creates the transaction type for check deposits.
     */
    private Mono<com.rtcomops.treasury.entity.TransactionType> findOrCreateDepositTransactionType() {
        return transactionTypeRepository.findByCode(DEPOSIT_TX_TYPE_CODE)
            .switchIfEmpty(
                transactionTypeRepository.findByCategory("CHECK")
                    .next()
                    .switchIfEmpty(
                        Mono.defer(() -> {
                            LOG.warn("No REM transaction type found, creating default");
                            com.rtcomops.treasury.entity.TransactionType defaultType =
                                com.rtcomops.treasury.entity.TransactionType.builder()
                                    .id(UUID.randomUUID())
                                    .code(DEPOSIT_TX_TYPE_CODE)
                                    .label("Remise de chèques")
                                    .category("CHECK")
                                    .description("Transaction pour remise de chèques en lot")
                                    .isActive(true)
                                    .isNew(true)
                                    .build();
                            return transactionTypeRepository.save(defaultType);
                        })
                    )
            );
    }

    /**
     * Generates a unique reference for the deposit transaction.
     * Format: REM-TX-YYYYMM-NNNN
     */
    private Mono<String> generateDepositTransactionReference() {
        String yearMonth = LocalDate.now().format(YEAR_MONTH_FORMATTER);
        String typeCode = "REM-TX";

        return sequenceRepository.incrementSequence(typeCode, yearMonth)
            .then(sequenceRepository.findLastSequence(typeCode, yearMonth))
            .map(sequence -> {
                String formattedSequence = String.format("%04d", sequence);
                return typeCode + "-" + yearMonth + "-" + formattedSequence;
            })
            .switchIfEmpty(Mono.error(new com.rtcomops.treasury.exception.BusinessException(
                "Impossible de générer une référence pour la transaction de remise.")));
    }

    /**
     * Updates all checks in the deposit to CASHED status.
     */
    private Mono<Void> cashChecksInDeposit(UUID depositId, LocalDate cashedDate) {
        return checkRepository.findByCheckDepositId(depositId)
            .flatMap(check -> {
                Check original = check.toBuilder().build();
                check.setStatus("CASHED");
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
                        "Encaissement automatique via rapprochement de la remise"
                    ).thenReturn(saved));
            })
            .then();
    }

    /**
     * Updates the bank account balance with the deposit total amount.
     */
    private Mono<Void> updateAccountBalanceForDeposit(UUID accountId, BigDecimal amount) {
        return accountRepository.findById(accountId)
            .flatMap(account -> {
                BigDecimal currentBalance = account.getCurrentBalance() != null
                    ? account.getCurrentBalance()
                    : BigDecimal.ZERO;

                // Deposit is always CREDIT (money coming in)
                BigDecimal newBalance = currentBalance.add(amount);
                account.setCurrentBalance(newBalance);
                account.setNew(false);

                LOG.info("Updating account {} balance for deposit: {} + {} = {}",
                    accountId, currentBalance, amount, newBalance);
                return accountRepository.save(account).then();
            });
    }

    /**
     * Creates a reconciliation match between the transaction and the statement line.
     */
    private Mono<Void> createMatchForDeposit(BankTransaction transaction, StatementLine line) {
        ReconciliationMatch match = matchMapper.toEntityFromAuto(
            line.getId(),
            transaction.getId(),
            null,
            transaction.getAmount(),
            BigDecimal.valueOf(100), // 100% confidence for manual reconciliation
            "MANUAL_DEPOSIT"
        );

        return matchRepository.save(match)
            .flatMap(savedMatch -> auditLogService.log(
                AuditModule.RECONCILIATION,
                AuditAction.MANUAL_MATCH,
                savedMatch.getId(),
                "Rapprochement Remise",
                null,
                savedMatch,
                String.format("Ligne %.2f rapprochée avec remise de chèques (Transaction %s)",
                    line.getAmount(), transaction.getReference())
            ).thenReturn(savedMatch))
            .then();
    }

    /**
     * Marks a statement line as matched.
     */
    private Mono<Void> markLineAsMatched(StatementLine line) {
        StatementLine updatedLine = lineMapper.markAsMatched(line);
        return lineRepository.save(updatedLine)
            .flatMap(sl -> updateStatementReconciledCount(sl.getBankStatementId()))
            .then();
    }
}
